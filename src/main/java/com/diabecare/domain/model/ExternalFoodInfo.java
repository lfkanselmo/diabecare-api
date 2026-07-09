package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExternalFoodInfo {

    private final String barcode;
    private String name;
    private String brand;
    private BigDecimal caloriesPer100g;
    private BigDecimal carbsPer100g;
    private BigDecimal proteinsPer100g;
    private BigDecimal fatsPer100g;
}
