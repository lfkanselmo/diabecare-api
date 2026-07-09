package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;

public record ExternalFoodResponse(
        String barcode,
        String name,
        String brand,
        BigDecimal caloriesPer100g,
        BigDecimal carbsPer100g,
        BigDecimal proteinsPer100g,
        BigDecimal fatsPer100g
) {}
