package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.MealEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MealEntryJpaRepository extends JpaRepository<MealEntryEntity, UUID> {
    List<MealEntryEntity> findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
            UUID patientId, LocalDateTime from, LocalDateTime to);
}