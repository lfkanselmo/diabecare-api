package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MenstrualCycleRequest(
        @NotNull LocalDate startDate,
        @Min(2) @Max(10) Integer periodLengthDays,
        String symptoms,
        String notes
) {}