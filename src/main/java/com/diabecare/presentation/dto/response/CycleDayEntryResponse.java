package com.diabecare.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CycleDayEntryResponse(
        String dayEntryId,
        LocalDate entryDate,
        String flowIntensity,
        String flowIntensityLabel,
        String notes,
        List<SymptomResponse> symptoms
) {
    public record SymptomResponse(
            String symptom,
            String symptomLabel,
            String severity
    ) {}
}