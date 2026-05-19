package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VitalSignResponse(
        UUID vitalId,
        BigDecimal weightKg,
        BigDecimal heightCm,
        BigDecimal bmi,
        String bmiCategory,
        Integer systolicBp,
        Integer diastolicBp,
        Integer heartRate,
        BigDecimal hba1c,
        LocalDateTime measuredAt,
        String notes
) {}