package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadAuditLogPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.domain.model.AuditLog;
import com.diabecare.infrastructure.persistence.mapper.AuditLogPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements SaveAuditLogPort, LoadAuditLogPort {

    private final AuditLogJpaRepository repository;
    private final AuditLogPersistenceMapper mapper;

    @Override
    public void save(AuditLog auditLog) {
        repository.save(mapper.toEntity(auditLog));
    }

    @Override
    public List<AuditLog> findByPatientId(UUID patientId) {
        return repository.findFirst200ByPatientIdOrderByPerformedAtDesc(patientId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByPatientIdAndEntityType(UUID patientId, String entityType) {
        return repository.findFirst200ByPatientIdAndEntityTypeOrderByPerformedAtDesc(patientId, entityType)
                .stream().map(mapper::toDomain).toList();
    }
}