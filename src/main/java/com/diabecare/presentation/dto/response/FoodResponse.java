package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record FoodResponse(
        UUID foodId,
        String name,
        String category,
        BigDecimal caloriesPer100g,
        BigDecimal carbsPer100g,
        BigDecimal proteinsPer100g,
        BigDecimal fatsPer100g,
        BigDecimal fiberPer100g,
        BigDecimal sodiumPer100g
) {}