package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.ExerciseLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ExerciseLogJpaRepository extends JpaRepository<ExerciseLogEntity, UUID> {
    List<ExerciseLogEntity> findByPatientIdAndPerformedAtBetweenOrderByPerformedAtDesc(
            UUID patientId, LocalDateTime from, LocalDateTime to);
    void deleteByPatientId(UUID patientId);
}