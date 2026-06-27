package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.CycleDayEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CycleDayEntryJpaRepository extends JpaRepository<CycleDayEntryEntity, UUID> {
    Optional<CycleDayEntryEntity> findByCycleIdAndEntryDate(UUID cycleId, LocalDate entryDate);
    List<CycleDayEntryEntity> findByCycleIdOrderByEntryDate(UUID cycleId);
    List<CycleDayEntryEntity> findByPatientIdAndEntryDateBetweenOrderByEntryDate(
            UUID patientId, LocalDate from, LocalDate to);
}