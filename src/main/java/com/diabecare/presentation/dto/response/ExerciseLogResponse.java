package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ExerciseLogResponse(
        UUID exerciseId,
        String exerciseType,
        String intensity,
        Integer durationMinutes,
        BigDecimal caloriesBurned,
        String notes,
        String performedAt
) {}