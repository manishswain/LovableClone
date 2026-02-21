package com.codingshuttle.projects.lovable_clone.service;

import com.codingshuttle.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.codingshuttle.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.codingshuttle.projects.lovable_clone.dto.subscription.PortalResponse;
import com.codingshuttle.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.codingshuttle.projects.lovable_clone.enums.SubscriptionStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();

    void activateSubscription(Long userId, Long planId, String subscriptionId, String stripeCustomerId);

    void updateSubscription(String id, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String id);

    void renewSubscriptionPeriod(String subscriptionId, Instant periodStart, Instant periodEnd);

    void markSubscriptionAsPastDue(String subscriptionId);

    boolean canCreateNewProject();
}
