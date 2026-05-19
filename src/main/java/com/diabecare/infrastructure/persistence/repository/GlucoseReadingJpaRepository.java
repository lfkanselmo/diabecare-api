package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.GlucoseReadingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GlucoseReadingJpaRepository extends JpaRepository<GlucoseReadingEntity, UUID> {
    List<GlucoseReadingEntity> findByPatientIdOrderByMeasuredAtDesc(UUID patientId);
    List<GlucoseReadingEntity> findByPatientIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
            UUID patientId, LocalDateTime from, LocalDateTime to);
}