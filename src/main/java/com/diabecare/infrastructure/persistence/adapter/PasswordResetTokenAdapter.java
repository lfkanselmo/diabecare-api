package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.PasswordResetTokenPort;
import com.diabecare.domain.model.PasswordResetToken;
import com.diabecare.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.diabecare.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenAdapter implements PasswordResetTokenPort {

    private static final int RAW_TOKEN_BYTES = 32;

    private final PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public IssuedToken issue(UUID userId, LocalDateTime expiresAt) {
        String rawToken = generateRawToken();

        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .userId(userId)
                .tokenHash(hash(rawToken))
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();

        passwordResetTokenJpaRepository.save(entity);

        return new IssuedToken(rawToken, expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByRawToken(String rawToken) {
        return passwordResetTokenJpaRepository.findByTokenHash(hash(rawToken))
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void markUsed(UUID tokenId) {
        passwordResetTokenJpaRepository.findById(tokenId).ifPresent(entity -> {
            entity.setUsedAt(LocalDateTime.now());
            passwordResetTokenJpaRepository.save(entity);
        });
    }

    private String generateRawToken() {
        byte[] bytes = new byte[RAW_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return PasswordResetToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tokenHash(entity.getTokenHash())
                .expiresAt(entity.getExpiresAt())
                .usedAt(entity.getUsedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
