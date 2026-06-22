package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PushSubscription {

    private UUID          id;
    private UUID          patientId;
    private String        endpoint;
    private String        p256dh;
    private String        auth;
    private LocalDateTime createdAt;
}