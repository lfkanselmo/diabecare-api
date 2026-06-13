package com.diabecare.application.port.in;

import com.diabecare.domain.model.AuditLog;

import java.util.List;
import java.util.UUID;

public interface GetAuditLogUseCase {
    List<AuditLog> getByPatient(UUID patientId);
    List<AuditLog> getByPatientAndEntity(UUID patientId, String entityType);
}