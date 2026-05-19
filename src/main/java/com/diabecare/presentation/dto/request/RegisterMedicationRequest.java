package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterMedicationRequest(
        @NotBlank
        String name,

        @NotNull
        String type,

        @NotNull @DecimalMin("0.01")
        BigDecimal dose,

        @NotNull
        String doseUnit,

        @NotNull
        String frequency,

        LocalDate startDate,
        String notes
) {}