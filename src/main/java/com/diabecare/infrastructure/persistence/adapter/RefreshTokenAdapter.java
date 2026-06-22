package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.infrastructure.config.JwtProperties;
import com.diabecare.infrastructure.persistence.entity.RefreshTokenEntity;
import com.diabecare.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenAdapter implements RefreshTokenPort {

    private static final int RAW_TOKEN_BYTES = 64;

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public IssuedToken issue(UUID userId, String deviceLabel) {
        String rawToken = generateRawToken();
        long expiresInMs = jwtProperties.getRefreshTokenExpiryMs();
        LocalDateTime now = LocalDateTime.now();

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .userId(userId)
                .tokenHash(hash(rawToken))
                .deviceLabel(deviceLabel)
                .lastUsedAt(now)
                .expiresAt(now.plusNanos(expiresInMs * 1_000_000L))
                .createdAt(now)
                .build();

        refreshTokenJpaRepository.save(entity);

        return new IssuedToken(rawToken, expiresInMs);
    }

    @Override
    @Transactional
    public Optional<RedeemedToken> redeem(String rawToken) {
        return refreshTokenJpaRepository.findByTokenHash(hash(rawToken))
                .filter(entity -> entity.getRevokedAt() == null)
                .filter(entity -> entity.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(entity -> {
                    entity.setRevokedAt(LocalDateTime.now());
                    refreshTokenJpaRepository.save(entity);
                    return new RedeemedToken(entity.getUserId(), entity.getDeviceLabel());
                });
    }

    @Override
    @Transactional
    public void revokeOne(String rawToken) {
        refreshTokenJpaRepository.findByTokenHash(hash(rawToken))
                .filter(entity -> entity.getRevokedAt() == null)
                .ifPresent(entity -> {
                    entity.setRevokedAt(LocalDateTime.now());
                    refreshTokenJpaRepository.save(entity);
                });
    }

    @Override
    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenJpaRepository.revokeAllActiveByUserId(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveSession> findActiveSessions(UUID userId) {
        return refreshTokenJpaRepository.findActiveByUserId(userId).stream()
                .map(entity -> new ActiveSession(
                        entity.getId(),
                        entity.getDeviceLabel(),
                        entity.getLastUsedAt(),
                        entity.getCreatedAt()
                ))
                .toList();
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
}