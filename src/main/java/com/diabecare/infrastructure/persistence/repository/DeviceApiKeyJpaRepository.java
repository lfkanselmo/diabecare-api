package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.DeviceApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceApiKeyJpaRepository extends JpaRepository<DeviceApiKeyEntity, UUID> {

    Optional<DeviceApiKeyEntity> findByKeyHash(String keyHash);

    List<DeviceApiKeyEntity> findAllByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
