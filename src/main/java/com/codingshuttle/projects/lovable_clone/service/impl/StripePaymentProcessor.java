package com.codingshuttle.projects.lovable_clone.service.impl;

import com.codingshuttle.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.codingshuttle.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.codingshuttle.projects.lovable_clone.dto.subscription.PortalResponse;
import com.codingshuttle.projects.lovable_clone.entity.Plan;
import com.codingshuttle.projects.lovable_clone.entity.User;
import com.codingshuttle.projects.lovable_clone.enums.SubscriptionStatus;
import com.codingshuttle.projects.lovable_clone.error.ResourceNotFoundException;
import com.codingshuttle.projects.lovable_clone.repository.PlanRepository;
import com.codingshuttle.projects.lovable_clone.repository.UserRepository;
import com.codingshuttle.projects.lovable_clone.security.AuthUtil;
import com.codingshuttle.projects.lovable_clone.service.PaymentProcessor;
import com.codingshuttle.projects.lovable_clone.service.SubscriptionService;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {

    private final AuthUtil authUtil;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Value("${client.url}")
    private String frontendUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan" , request.planId().toString()));
        Long userId = authUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(plan.getStripePriceId()).setQuantity(1L).build()
                )
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build()
                                ).build()
                )
                .setSuccessUrl(frontendUrl+"/sucess.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl+"/cancel.html")
                .putMetadata("user_id", userId.toString())
                .putMetadata("plan_id", plan.getId().toString());


        try {
            String stripeCustomerId = user.getStripeCustomerId();
            if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                params.setCustomerEmail(user.getUsername());
            }else{
                params.setCustomer(stripeCustomerId);
            }
            Session session = Session.create(params.build());

            return new CheckoutResponse(session.getUrl()); // making API call to Stripe
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PortalResponse openCustomerPortal() {
        return null;
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.debug("Handling stripe event: {}", type);

        switch (type) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject, metadata); // one-time, on checkout completed
            case "customer.subscription.updated" -> handleCustomerSubscriptionUpdated((Subscription) stripeObject); // when user cancels, upgrades or any updates
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted((Subscription) stripeObject); // when subscription ends, revoke the access
            case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject); // when invoice is paid
            case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject); // when invoice is not paid, mark as PAST_DUE
            default -> log.debug("Ignoring the event: {}", type);
        }
    }


    private void handleCheckoutSessionCompleted(Session session, Map<String, String> metadata) {
        if(session == null){
            log.error("Session object is null in checkout.session.completed event");
            return;
        }
        Long userId = Long.parseLong(metadata.get("user_id"));
        Long planId = Long.parseLong(metadata.get("plan_id"));

        String subscriptionId = session.getSubscription();
        String stripeCustomerId = session.getCustomer();

        User user = getUser(userId);
        if(user.getStripeCustomerId() == null){
            user.setStripeCustomerId(stripeCustomerId);
            userRepository.save(user);
        }

        subscriptionService.activateSubscription(userId, planId, subscriptionId, stripeCustomerId);
    }

    private void handleCustomerSubscriptionUpdated(Subscription subscription) {
        if(subscription == null) {
            log.error("subscription object was null inside handleCustomerSubscriptionUpdated");
            return;
        }

        SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
        if (status == null) {
            log.warn("Unknown status '{}' for subscription {}", subscription.getStatus(), subscription.getId());
            return;
        }

        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant periodStart = toInstant(item.getCurrentPeriodStart());
        Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

        Long planId = resolvePlanId(item.getPrice());

        subscriptionService.updateSubscription(subscription.getId(), status, periodStart, periodEnd, subscription.getCancelAtPeriodEnd(),planId);
    }

    private void handleCustomerSubscriptionDeleted(Subscription subscription) {
        if (subscription == null) {
            log.error("subscription object was null inside handleCustomerSubscriptionDeleted");
            return;
        }
        subscriptionService.cancelSubscription(subscription.getId());
    }


    private void handleInvoicePaid(Invoice invoice) {
        String subscriptionId = extractSubscriptionId(invoice);
        if (subscriptionId == null) return;

        try{
            Subscription subscription = Subscription.retrieve(subscriptionId);
            var item = subscription.getItems().getData().get(0);

            Instant periodStart = toInstant(item.getCurrentPeriodStart());
            Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(subscriptionId, periodStart, periodEnd);
        }catch (StripeException e) {
            log.error("Failed to retrieve subscription {} for invoice {}: {}", subscriptionId, invoice.getId(), e.getMessage());

        }
    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
        String subscriptionId = extractSubscriptionId(invoice);
        if (subscriptionId == null) return;

        subscriptionService.markSubscriptionAsPastDue(subscriptionId);
    }

    // Utility Methods

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("user", userId.toString()));
    }

    private SubscriptionStatus mapStripeStatusToEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status: {}", status);
                yield null;
            }
        };
    }

    private Instant toInstant(Long epoch) {
        return epoch != null ? Instant.ofEpochSecond(epoch) : null;
    }

    private Long resolvePlanId(Price price) {
        if (price == null || price.getId() == null) return null;
        return planRepository.findByStripePriceId(price.getId())
                .map(Plan::getId)
                .orElse(null);
    }

    private String extractSubscriptionId(Invoice invoice) {
        var parent = invoice.getParent();
        if (parent == null) return null;

        var subDetails = parent.getSubscriptionDetails();
        if (subDetails == null) return null;

        return subDetails.getSubscription();
    }
}
