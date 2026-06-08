package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdatePatientRequest(
        @DecimalMin("50") @DecimalMax("250")
        BigDecimal heightCm,

        @DecimalMin("50")
        BigDecimal targetGlucoseMin,

        @DecimalMin("50")
        BigDecimal targetGlucoseMax,

        @Min(500) @Max(5000)
        Integer dailyCalorieGoal,

        String activityLevel,
        String preferredGlucoseUnit
) {}