package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class MealItem {

    private final UUID mealItemId;
    private String foodName;
    private BigDecimal quantityGrams;
    private BigDecimal calories;
    private BigDecimal carbohydrates;
    private BigDecimal proteins;
    private BigDecimal fats;
    private String foodCode;

    public static MealItem create(
            String foodName,
            BigDecimal quantityGrams,
            BigDecimal calories,
            BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats,
            String foodCode
    ) {
        return MealItem.builder()
                .mealItemId(UUID.randomUUID())
                .foodName(foodName)
                .quantityGrams(quantityGrams)
                .calories(calories)
                .carbohydrates(carbohydrates)
                .proteins(proteins)
                .fats(fats)
                .foodCode(foodCode)
                .build();
    }
}