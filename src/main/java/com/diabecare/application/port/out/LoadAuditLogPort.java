package com.diabecare.application.port.out;

import com.diabecare.domain.model.AuditLog;

import java.util.List;
import java.util.UUID;

public interface LoadAuditLogPort {
    List<AuditLog> findByPatientId(UUID patientId);
    List<AuditLog> findByPatientIdAndEntityType(UUID patientId, String entityType);
}