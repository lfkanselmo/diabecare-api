package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetAuditLogUseCase;
import com.diabecare.application.port.out.LoadAuditLogPort;
import com.diabecare.domain.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAuditLogUseCaseImpl implements GetAuditLogUseCase {

    private final LoadAuditLogPort loadAuditLogPort;

    @Override
    public List<AuditLog> getByPatient(UUID patientId) {
        return loadAuditLogPort.findByPatientId(patientId);
    }

    @Override
    public List<AuditLog> getByPatientAndEntity(UUID patientId, String entityType) {
        return loadAuditLogPort.findByPatientIdAndEntityType(patientId, entityType);
    }
}