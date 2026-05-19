package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummaryResponse(
        LocalDate date,
        BigDecimal totalCalories,
        BigDecimal totalCarbohydrates,
        BigDecimal totalProteins,
        BigDecimal totalFats,
        Integer calorieGoal,
        boolean goalReached
) {}