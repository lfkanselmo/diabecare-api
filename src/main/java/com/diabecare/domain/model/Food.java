package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class Food {

    private final UUID foodId;
    private String name;
    private String category;
    private BigDecimal caloriesPer100g;
    private BigDecimal carbsPer100g;
    private BigDecimal proteinsPer100g;
    private BigDecimal fatsPer100g;
    private BigDecimal fiberPer100g;
    private BigDecimal sodiumPer100g;
}