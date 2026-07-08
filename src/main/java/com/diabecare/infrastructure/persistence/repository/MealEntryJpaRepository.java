package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.MealEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MealEntryJpaRepository extends JpaRepository<MealEntryEntity, UUID> {

    // Sin paginar: usado por resúmenes/estadísticas/exportación, que necesitan
    // el rango completo. JOIN FETCH aquí es seguro porque no hay Pageable.
    @Query("SELECT DISTINCT m FROM MealEntryEntity m LEFT JOIN FETCH m.items " +
            "WHERE m.patientId = :patientId AND m.consumedAt BETWEEN :from AND :to " +
            "ORDER BY m.consumedAt DESC")
    List<MealEntryEntity> findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
            @Param("patientId") UUID patientId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Sin JOIN FETCH a propósito: con Pageable, Hibernate paginaría el fetch-join
    // en memoria. items se completa vía @BatchSize en MealEntryEntity.
    Page<MealEntryEntity> findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
            UUID patientId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    void deleteByPatientId(UUID patientId);
}