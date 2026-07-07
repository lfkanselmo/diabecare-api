package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CaregiverLink {

    private UUID              id;
    private UUID              patientId;
    private UUID              caregiverUserId;
    private CaregiverLinkStatus status;
    private LocalDateTime     createdAt;
    private LocalDateTime     revokedAt;

    public static CaregiverLink create(UUID patientId, UUID caregiverUserId) {
        return CaregiverLink.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .caregiverUserId(caregiverUserId)
                .status(CaregiverLinkStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isActive() {
        return status == CaregiverLinkStatus.ACTIVE;
    }

    public void revoke() {
        this.status = CaregiverLinkStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
    }
}
