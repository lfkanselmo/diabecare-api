package com.diabecare.domain.service;

import com.diabecare.domain.model.AuditLog;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    public AuditLog buildUpdateLog(UUID patientId, String entityType,
                                   UUID entityId, String fieldName,
                                   String oldValue, String newValue) {
        return AuditLog.builder()
                .patientId(patientId)
                .entityType(entityType)
                .entityId(entityId)
                .action(AuditLog.Action.UPDATE)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }

    public AuditLog buildCreateLog(UUID patientId, String entityType, UUID entityId) {
        return AuditLog.builder()
                .patientId(patientId)
                .entityType(entityType)
                .entityId(entityId)
                .action(AuditLog.Action.CREATE)
                .build();
    }

    public AuditLog buildDeleteLog(UUID patientId, String entityType, UUID entityId) {
        return AuditLog.builder()
                .patientId(patientId)
                .entityType(entityType)
                .entityId(entityId)
                .action(AuditLog.Action.DELETE)
                .build();
    }
}