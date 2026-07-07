package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.VitalSignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VitalSignJpaRepository extends JpaRepository<VitalSignEntity, UUID> {
    List<VitalSignEntity> findFirst500ByPatientIdOrderByMeasuredAtDesc(UUID patientId);
    Optional<VitalSignEntity> findFirstByPatientIdOrderByMeasuredAtDesc(UUID patientId);
    void deleteByPatientId(UUID patientId);
}