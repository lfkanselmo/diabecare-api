package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;

public record SystemConfigResponse(
        String        key,
        String        value,
        String        dataType,
        String        category,
        String        description,
        LocalDateTime updatedAt
) {}