package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidMealEntryException;
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
        return createWithId(UUID.randomUUID(), foodName, quantityGrams, calories, carbohydrates, proteins, fats, foodCode);
    }

    /** Igual que {@link #create}, honrando un ID provisto por el cliente — ver {@link GlucoseReading#createWithId}. */
    public static MealItem createWithId(
            UUID mealItemId,
            String foodName,
            BigDecimal quantityGrams,
            BigDecimal calories,
            BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats,
            String foodCode
    ) {
        validateFoodName(foodName);
        validateQuantity(quantityGrams);
        validateRequiredNonNegative(calories, "Las calorías");
        validateRequiredNonNegative(carbohydrates, "Los carbohidratos");
        validateOptionalNonNegative(proteins, "Las proteínas");
        validateOptionalNonNegative(fats, "Las grasas");

        return MealItem.builder()
                .mealItemId(mealItemId)
                .foodName(foodName)
                .quantityGrams(quantityGrams)
                .calories(calories)
                .carbohydrates(carbohydrates)
                .proteins(proteins)
                .fats(fats)
                .foodCode(foodCode)
                .build();
    }

    private static void validateFoodName(String foodName) {
        if (foodName == null || foodName.isBlank()) {
            throw new InvalidMealEntryException("El nombre del alimento es obligatorio");
        }
    }

    private static void validateQuantity(BigDecimal quantityGrams) {
        if (quantityGrams == null || quantityGrams.compareTo(BigDecimal.valueOf(0.1)) < 0) {
            throw new InvalidMealEntryException("La cantidad debe ser mayor o igual a 0.1 gramos");
        }
    }

    private static void validateRequiredNonNegative(BigDecimal value, String fieldLabel) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMealEntryException(fieldLabel + " son obligatorias y no pueden ser negativas");
        }
    }

    private static void validateOptionalNonNegative(BigDecimal value, String fieldLabel) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMealEntryException(fieldLabel + " no pueden ser negativas");
        }
    }
}