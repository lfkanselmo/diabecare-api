package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.GlucoseReadingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GlucoseReadingJpaRepository extends JpaRepository<GlucoseReadingEntity, UUID> {
    // Sin paginar: usado por estadísticas/AGP/alertas/exportación, que necesitan
    // el rango completo para calcular agregados correctos, no una página.
    List<GlucoseReadingEntity> findByPatientIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
            UUID patientId, LocalDateTime from, LocalDateTime to);
    Page<GlucoseReadingEntity> findByPatientIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
            UUID patientId, LocalDateTime from, LocalDateTime to, Pageable pageable);
    Optional<GlucoseReadingEntity> findFirstByPatientIdOrderByMeasuredAtDesc(UUID patientId);
    void deleteByPatientId(UUID patientId);
}