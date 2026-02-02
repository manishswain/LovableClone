package com.codingshuttle.projects.lovable_clone.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        // Initialize Stripe with the secret key
        Stripe.apiKey = stripeSecretKey;
    }
}
