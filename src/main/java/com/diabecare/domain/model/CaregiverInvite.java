package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CaregiverInvite {

    private UUID          id;
    private UUID          patientId;
    private String        codeHash;
    private LocalDateTime expiresAt;
    private LocalDateTime redeemedAt;
    private UUID          redeemedByUserId;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isRedeemed() {
        return redeemedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isRevoked() && !isRedeemed();
    }
}
