package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.CaregiverInvitePort;
import com.diabecare.domain.model.CaregiverInvite;
import com.diabecare.infrastructure.persistence.entity.CaregiverInviteEntity;
import com.diabecare.infrastructure.persistence.repository.CaregiverInviteJpaRepository;
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
public class CaregiverInviteAdapter implements CaregiverInvitePort {

    // Sin 0/O/1/I para evitar ambigüedad al compartir el código verbalmente o por escrito.
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final CaregiverInviteJpaRepository caregiverInviteJpaRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public IssuedInvite issue(UUID patientId, LocalDateTime expiresAt) {
        String rawCode = generateCode();

        CaregiverInviteEntity entity = CaregiverInviteEntity.builder()
                .patientId(patientId)
                .codeHash(hash(rawCode))
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();

        caregiverInviteJpaRepository.save(entity);

        return new IssuedInvite(formatForDisplay(rawCode), expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CaregiverInvite> findByRawCode(String rawCode) {
        return caregiverInviteJpaRepository.findByCodeHash(hash(normalize(rawCode)))
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void markRedeemed(UUID inviteId, UUID redeemedByUserId) {
        caregiverInviteJpaRepository.findById(inviteId).ifPresent(entity -> {
            entity.setRedeemedAt(LocalDateTime.now());
            entity.setRedeemedByUserId(redeemedByUserId);
            caregiverInviteJpaRepository.save(entity);
        });
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    // El guion es solo cosmético para facilitar compartir/leer el código — normalize()
    // lo quita antes de calcular el hash, así que da igual si se reintroduce al redimir.
    private String formatForDisplay(String rawCode) {
        return rawCode.substring(0, 4) + "-" + rawCode.substring(4);
    }

    private String normalize(String rawCode) {
        return rawCode.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private CaregiverInvite toDomain(CaregiverInviteEntity entity) {
        return CaregiverInvite.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .codeHash(entity.getCodeHash())
                .expiresAt(entity.getExpiresAt())
                .redeemedAt(entity.getRedeemedAt())
                .redeemedByUserId(entity.getRedeemedByUserId())
                .revokedAt(entity.getRevokedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
