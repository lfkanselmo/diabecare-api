package com.diabecare.application.port.out;

import com.diabecare.domain.model.DeviceApiKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceApiKeyPort {

    IssuedKey issue(UUID patientId, String label);

    Optional<DeviceApiKey> findByRawKey(String rawKey);

    List<DeviceApiKey> findAllByPatientId(UUID patientId);

    void revoke(UUID patientId, UUID keyId);

    void touchLastUsed(UUID keyId);

    record IssuedKey(UUID id, String rawKey, String label, LocalDateTime createdAt) {}
}
