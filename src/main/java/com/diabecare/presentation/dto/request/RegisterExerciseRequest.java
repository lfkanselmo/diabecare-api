package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegisterExerciseRequest(
        @NotNull String exerciseType,
        @NotNull String intensity,
        @NotNull @Min(1) Integer durationMinutes,
        String notes,
        String performedAt,
        @DecimalMin("0") BigDecimal caloriesBurned
) {}