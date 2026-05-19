package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.MedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicationJpaRepository extends JpaRepository<MedicationEntity, UUID> {
    List<MedicationEntity> findByPatientIdAndActiveTrue(UUID patientId);
    List<MedicationEntity> findByPatientId(UUID patientId);
}