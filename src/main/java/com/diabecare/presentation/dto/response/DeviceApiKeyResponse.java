package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeviceApiKeyResponse(
        UUID id,
        String label,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt,
        boolean revoked
) {}
