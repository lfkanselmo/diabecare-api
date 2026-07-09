package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.MobilePushTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MobilePushTokenJpaRepository extends JpaRepository<MobilePushTokenEntity, UUID> {
    List<MobilePushTokenEntity> findAllByPatientId(UUID patientId);
    void deleteByDeviceToken(String deviceToken);
}
