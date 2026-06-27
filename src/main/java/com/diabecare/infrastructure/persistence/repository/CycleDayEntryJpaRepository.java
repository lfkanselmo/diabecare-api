package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.CycleDayEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CycleDayEntryJpaRepository extends JpaRepository<CycleDayEntryEntity, UUID> {

    Optional<CycleDayEntryEntity> findByCycleIdAndEntryDate(UUID cycleId, LocalDate entryDate);

    @Query("SELECT DISTINCT d FROM CycleDayEntryEntity d LEFT JOIN FETCH d.symptoms " +
            "WHERE d.cycleId = :cycleId ORDER BY d.entryDate")
    List<CycleDayEntryEntity> findByCycleIdOrderByEntryDate(@Param("cycleId") UUID cycleId);

    @Query("SELECT DISTINCT d FROM CycleDayEntryEntity d LEFT JOIN FETCH d.symptoms " +
            "WHERE d.patientId = :patientId AND d.entryDate BETWEEN :from AND :to " +
            "ORDER BY d.entryDate")
    List<CycleDayEntryEntity> findByPatientIdAndEntryDateBetweenOrderByEntryDate(
            @Param("patientId") UUID patientId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}