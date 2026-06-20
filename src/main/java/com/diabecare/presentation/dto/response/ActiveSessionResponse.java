package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActiveSessionResponse(
        UUID id,
        String deviceLabel,
        LocalDateTime lastUsedAt,
        LocalDateTime createdAt
) {}