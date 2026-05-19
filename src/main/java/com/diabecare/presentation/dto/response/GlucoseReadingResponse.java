package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GlucoseReadingResponse(
        UUID readingId,
        BigDecimal value,
        String unit,
        String readingType,
        String status,
        LocalDateTime measuredAt,
        String notes,
        String deviceSource
) {}