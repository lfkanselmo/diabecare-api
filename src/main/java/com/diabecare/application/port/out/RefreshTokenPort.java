package com.diabecare.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenPort {

    record IssuedToken(String rawToken, long expiresInMs) {}

    record RedeemedToken(UUID userId, String deviceLabel) {}

    record ActiveSession(
            UUID id,
            String deviceLabel,
            LocalDateTime lastUsedAt,
            LocalDateTime createdAt
    ) {}

    IssuedToken issue(UUID userId, String deviceLabel);

    Optional<RedeemedToken> redeem(String rawToken);

    void revokeOne(String rawToken);

    void revokeAllForUser(UUID userId);

    List<ActiveSession> findActiveSessions(UUID userId);
}