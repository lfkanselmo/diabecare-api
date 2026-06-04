package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;

public record InsulinCalculationResponse(
        BigDecimal correctionDose,
        BigDecimal mealDose,
        BigDecimal totalDose,
        String explanation
) {}