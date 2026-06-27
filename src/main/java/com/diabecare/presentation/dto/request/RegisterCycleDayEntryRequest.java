package com.diabecare.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record RegisterCycleDayEntryRequest(
        @NotNull LocalDate entryDate,
        @NotNull String flowIntensity,
        String notes,
        @Valid List<SymptomInput> symptoms
) {
    public record SymptomInput(
            @NotNull String symptom,
            @NotNull String severity
    ) {}
}