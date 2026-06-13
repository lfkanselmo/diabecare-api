package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID          id,
        String        entityType,
        UUID          entityId,
        String        action,
        String        fieldName,
        String        oldValue,
        String        newValue,
        LocalDateTime performedAt
) {}