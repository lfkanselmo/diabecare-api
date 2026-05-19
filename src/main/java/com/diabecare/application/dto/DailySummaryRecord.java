package com.diabecare.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummaryRecord(
        LocalDate date,
        BigDecimal totalCalories,
        BigDecimal totalCarbohydrates,
        BigDecimal totalProteins,
        BigDecimal totalFats,
        Integer calorieGoal,
        boolean goalReached
) {}