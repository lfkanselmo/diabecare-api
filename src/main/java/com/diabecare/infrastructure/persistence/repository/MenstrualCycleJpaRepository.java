package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.MenstrualCycleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenstrualCycleJpaRepository extends JpaRepository<MenstrualCycleEntity, UUID> {
    List<MenstrualCycleEntity> findByPatientIdOrderByStartDateDesc(UUID patientId);
    Optional<MenstrualCycleEntity> findFirstByPatientIdOrderByStartDateDesc(UUID patientId);
}