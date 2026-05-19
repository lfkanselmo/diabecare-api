package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MealEntryResponse(
        UUID mealId,
        String mealType,
        LocalDateTime consumedAt,
        String notes,
        BigDecimal totalCalories,
        BigDecimal totalCarbohydrates,
        BigDecimal totalProteins,
        BigDecimal totalFats,
        List<MealItemResponse> items
) {
    public record MealItemResponse(
            UUID mealItemId,
            String foodName,
            BigDecimal quantityGrams,
            BigDecimal calories,
            BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats
    ) {}
}