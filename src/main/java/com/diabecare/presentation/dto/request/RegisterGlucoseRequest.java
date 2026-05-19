package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegisterGlucoseRequest(
        @NotNull @DecimalMin("20") @DecimalMax("600")
        BigDecimal value,

        @NotNull
        String unit,

        @NotNull
        String readingType,

        @NotNull
        LocalDateTime measuredAt,

        String notes,
        String deviceSource
) {}