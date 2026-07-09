package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MobilePushToken {

    private UUID id;
    private UUID patientId;
    private String deviceToken;
    private MobilePlatform platform;
    private LocalDateTime createdAt;
}
