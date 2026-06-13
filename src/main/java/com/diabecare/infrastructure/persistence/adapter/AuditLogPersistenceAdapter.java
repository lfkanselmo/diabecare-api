package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadAuditLogPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.domain.model.AuditLog;
import com.diabecare.infrastructure.persistence.entity.AuditLogEntity;
import com.diabecare.infrastructure.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements SaveAuditLogPort, LoadAuditLogPort {

    private final AuditLogJpaRepository repository;

    @Override
    public void save(AuditLog auditLog) {
        repository.save(toEntity(auditLog));
    }

    @Override
    public List<AuditLog> findByPatientId(UUID patientId) {
        return repository.findByPatientIdOrderByPerformedAtDesc(patientId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByPatientIdAndEntityType(UUID patientId, String entityType) {
        return repository.findByPatientIdAndEntityTypeOrderByPerformedAtDesc(patientId, entityType)
                .stream().map(this::toDomain).toList();
    }

    private AuditLogEntity toEntity(AuditLog log) {
        return AuditLogEntity.builder()
                .patientId(log.getPatientId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction().name())
                .fieldName(log.getFieldName())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .build();
    }

    private AuditLog toDomain(AuditLogEntity entity) {
        return AuditLog.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .action(AuditLog.Action.valueOf(entity.getAction()))
                .fieldName(entity.getFieldName())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .performedAt(entity.getPerformedAt())
                .build();
    }
}