package com.diabecare.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RegisterMealRequest(
        @NotNull
        String mealType,

        @NotNull
        LocalDateTime consumedAt,

        String notes,

        @NotNull @NotEmpty @Valid
        List<MealItemRequest> items,

        // Opcionales — ver RegisterGlucoseRequest.readingId para el porqué.
        UUID mealId
) {
    public record MealItemRequest(
            @NotNull String foodName,
            @NotNull @DecimalMin("0.1") BigDecimal quantityGrams,
            @NotNull @DecimalMin("0") BigDecimal calories,
            @NotNull @DecimalMin("0") BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats,
            String foodCode,
            UUID mealItemId
    ) {}
}