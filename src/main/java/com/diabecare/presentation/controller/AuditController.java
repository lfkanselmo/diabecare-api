package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetAuditLogUseCase;
import com.diabecare.domain.model.AuditLog;
import com.diabecare.presentation.dto.response.AuditLogResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoría")
public class AuditController {

    private final GetAuditLogUseCase getAuditLogUseCase;

    @GetMapping("/{patientId}")
    public ResponseEntity<List<AuditLogResponse>> getByPatient(
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(toResponse(
                getAuditLogUseCase.getByPatient(patientId)));
    }

    @GetMapping("/{patientId}/{entityType}")
    public ResponseEntity<List<AuditLogResponse>> getByPatientAndEntity(
            @PathVariable UUID patientId,
            @PathVariable String entityType) {
        return ResponseEntity.ok(toResponse(
                getAuditLogUseCase.getByPatientAndEntity(patientId, entityType.toUpperCase())));
    }

    private List<AuditLogResponse> toResponse(List<AuditLog> logs) {
        return logs.stream().map(log -> new AuditLogResponse(
                log.getId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction().name(),
                log.getFieldName(),
                log.getOldValue(),
                log.getNewValue(),
                log.getPerformedAt()
        )).toList();
    }
}