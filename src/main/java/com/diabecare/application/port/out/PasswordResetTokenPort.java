package com.diabecare.application.port.out;

import com.diabecare.domain.model.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenPort {

    record IssuedToken(String rawToken, LocalDateTime expiresAt) {}

    IssuedToken issue(UUID userId, LocalDateTime expiresAt);

    Optional<PasswordResetToken> findByRawToken(String rawToken);

    void markUsed(UUID tokenId);
}
