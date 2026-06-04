package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InsulinCalculationRequest(
        @NotNull @DecimalMin("20")
        BigDecimal currentGlucose,

        BigDecimal carbsToEat,

        boolean beforeMeal
) {}