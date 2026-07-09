package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

// Es la única vez que se expone rawKey — a partir de aquí solo se guarda su hash.
public record GeneratedDeviceApiKeyResponse(
        UUID id,
        String rawKey,
        String label,
        LocalDateTime createdAt
) {}
