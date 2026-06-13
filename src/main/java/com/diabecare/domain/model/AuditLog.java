package com.diabecare.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AuditLog {

    public enum Action { CREATE, UPDATE, DELETE }

    private UUID          id;
    private UUID          patientId;
    private String        entityType;
    private UUID          entityId;
    private Action        action;
    private String        fieldName;
    private String        oldValue;
    private String        newValue;
    private LocalDateTime performedAt;
}