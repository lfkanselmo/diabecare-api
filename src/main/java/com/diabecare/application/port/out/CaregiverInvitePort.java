package com.diabecare.application.port.out;

import com.diabecare.domain.model.CaregiverInvite;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CaregiverInvitePort {

    record IssuedInvite(String rawCode, LocalDateTime expiresAt) {}

    IssuedInvite issue(UUID patientId, LocalDateTime expiresAt);

    Optional<CaregiverInvite> findByRawCode(String rawCode);

    void markRedeemed(UUID inviteId, UUID redeemedByUserId);
}
