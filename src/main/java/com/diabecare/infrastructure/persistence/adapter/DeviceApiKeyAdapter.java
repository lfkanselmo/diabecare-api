package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.DeviceApiKeyPort;
import com.diabecare.domain.model.DeviceApiKey;
import com.diabecare.infrastructure.persistence.entity.DeviceApiKeyEntity;
import com.diabecare.infrastructure.persistence.repository.DeviceApiKeyJpaRepository;
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
public class DeviceApiKeyAdapter implements DeviceApiKeyPort {

    private static final String KEY_PREFIX = "dbc_";
    private static final int KEY_BYTES = 32;

    private final DeviceApiKeyJpaRepository deviceApiKeyJpaRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public IssuedKey issue(UUID patientId, String label) {
        String rawKey = generateKey();
        LocalDateTime now = LocalDateTime.now();

        DeviceApiKeyEntity entity = DeviceApiKeyEntity.builder()
                .patientId(patientId)
                .label(label)
                .keyHash(hash(rawKey))
                .createdAt(now)
                .build();

        DeviceApiKeyEntity saved = deviceApiKeyJpaRepository.save(entity);

        return new IssuedKey(saved.getId(), rawKey, label, now);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeviceApiKey> findByRawKey(String rawKey) {
        return deviceApiKeyJpaRepository.findByKeyHash(hash(rawKey)).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceApiKey> findAllByPatientId(UUID patientId) {
        return deviceApiKeyJpaRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void revoke(UUID patientId, UUID keyId) {
        deviceApiKeyJpaRepository.findById(keyId)
                .filter(entity -> entity.getPatientId().equals(patientId))
                .ifPresent(entity -> {
                    entity.setRevokedAt(LocalDateTime.now());
                    deviceApiKeyJpaRepository.save(entity);
                });
    }

    @Override
    @Transactional
    public void touchLastUsed(UUID keyId) {
        deviceApiKeyJpaRepository.findById(keyId).ifPresent(entity -> {
            entity.setLastUsedAt(LocalDateTime.now());
            deviceApiKeyJpaRepository.save(entity);
        });
    }

    // Prefijo "dbc_" (DiabeCare) visible en texto plano para que se pueda reconocer
    // a simple vista en un log o config file — el secreto real es lo que sigue.
    private String generateKey() {
        byte[] randomBytes = new byte[KEY_BYTES];
        secureRandom.nextBytes(randomBytes);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
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

    private DeviceApiKey toDomain(DeviceApiKeyEntity entity) {
        return DeviceApiKey.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .label(entity.getLabel())
                .keyHash(entity.getKeyHash())
                .createdAt(entity.getCreatedAt())
                .lastUsedAt(entity.getLastUsedAt())
                .revokedAt(entity.getRevokedAt())
                .build();
    }
}
