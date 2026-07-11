package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.ExerciseLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ExerciseLogJpaRepository extends JpaRepository<ExerciseLogEntity, UUID> {
    // Sin paginar: usado por correlación con glucosa y exportación.
    List<ExerciseLogEntity> findByPatientIdAndPerformedAtBetweenOrderByPerformedAtDesc(
            UUID patientId, LocalDateTime from, LocalDateTime to);
    Page<ExerciseLogEntity> findByPatientIdAndPerformedAtBetweenOrderByPerformedAtDesc(
            UUID patientId, LocalDateTime from, LocalDateTime to, Pageable pageable);
    void deleteByPatientId(UUID patientId);

    // Cursor de sincronización incremental para el móvil offline-first.
    List<ExerciseLogEntity> findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc(
            UUID patientId, LocalDateTime since);
}