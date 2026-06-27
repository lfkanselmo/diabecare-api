package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.MealEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MealEntryJpaRepository extends JpaRepository<MealEntryEntity, UUID> {

    @Query("SELECT DISTINCT m FROM MealEntryEntity m LEFT JOIN FETCH m.items " +
            "WHERE m.patientId = :patientId AND m.consumedAt BETWEEN :from AND :to " +
            "ORDER BY m.consumedAt DESC")
    List<MealEntryEntity> findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
            @Param("patientId") UUID patientId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}