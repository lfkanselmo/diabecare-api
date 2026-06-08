package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RegisterExerciseRequest(
        @NotNull String exerciseType,
        @NotNull String intensity,
        @NotNull @Min(1) Integer durationMinutes,
        String notes,
        String performedAt
) {}