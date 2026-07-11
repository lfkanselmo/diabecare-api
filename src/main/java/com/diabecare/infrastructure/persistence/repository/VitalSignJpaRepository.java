package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.VitalSignEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VitalSignJpaRepository extends JpaRepository<VitalSignEntity, UUID> {
    // Usado solo por la exportación de datos (Habeas Data): ahí sí necesitamos
    // el historial completo, no una página — de ahí el límite de seguridad de 500
    // en vez de paginación real.
    List<VitalSignEntity> findFirst500ByPatientIdOrderByMeasuredAtDesc(UUID patientId);
    Page<VitalSignEntity> findByPatientIdOrderByMeasuredAtDesc(UUID patientId, Pageable pageable);
    Optional<VitalSignEntity> findFirstByPatientIdOrderByMeasuredAtDesc(UUID patientId);
    void deleteByPatientId(UUID patientId);

    // Cursor de sincronización incremental para el móvil offline-first.
    List<VitalSignEntity> findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc(
            UUID patientId, LocalDateTime since);
}