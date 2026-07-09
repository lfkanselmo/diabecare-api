package com.diabecare.presentation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterVitalSignRequest(
        BigDecimal weightKg,
        BigDecimal heightCm,
        Integer systolicBp,
        Integer diastolicBp,
        Integer heartRate,
        BigDecimal hba1c,
        LocalDateTime measuredAt,
        String notes,

        // Opcional — ver RegisterGlucoseRequest.readingId para el porqué.
        UUID vitalId
) {}