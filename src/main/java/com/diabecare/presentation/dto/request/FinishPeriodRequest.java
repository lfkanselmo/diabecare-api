package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FinishPeriodRequest(
        @NotNull LocalDate endDate
) {}