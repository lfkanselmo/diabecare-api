package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, UUID> {
    List<AuditLogEntity> findFirst200ByPatientIdOrderByPerformedAtDesc(UUID patientId);
    List<AuditLogEntity> findFirst200ByPatientIdAndEntityTypeOrderByPerformedAtDesc(
            UUID patientId, String entityType);
}