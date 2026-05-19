package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MedicationResponse(
        UUID medicationId,
        String name,
        String type,
        BigDecimal dose,
        String doseUnit,
        String frequency,
        LocalDate startDate,
        boolean active,
        String notes
) {}