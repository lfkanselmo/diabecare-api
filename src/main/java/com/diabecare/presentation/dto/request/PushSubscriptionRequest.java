package com.diabecare.presentation.dto.request;

public record PushSubscriptionRequest(
        String endpoint,
        String p256dh,
        String auth
) {}