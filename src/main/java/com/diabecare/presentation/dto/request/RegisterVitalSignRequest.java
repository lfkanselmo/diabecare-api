package com.diabecare.presentation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegisterVitalSignRequest(
        BigDecimal weightKg,
        BigDecimal heightCm,
        Integer systolicBp,
        Integer diastolicBp,
        Integer heartRate,
        BigDecimal hba1c,
        LocalDateTime measuredAt,
        String notes
) {}