package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidMealEntryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MealItem")
class MealItemTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("crea un alimento válido con todos los campos")
        void createsValidItem() {
            MealItem item = MealItem.create(
                    "Manzana", BigDecimal.valueOf(150), BigDecimal.valueOf(80),
                    BigDecimal.valueOf(20), BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.3),
                    "FOOD-001");

            assertThat(item.getMealItemId()).isNotNull();
            assertThat(item.getFoodName()).isEqualTo("Manzana");
            assertThat(item.getQuantityGrams()).isEqualByComparingTo(BigDecimal.valueOf(150));
            assertThat(item.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(80));
            assertThat(item.getFoodCode()).isEqualTo("FOOD-001");
        }

        @Test
        @DisplayName("crea un alimento válido sin proteínas ni grasas (opcionales)")
        void createsValidItemWithoutOptionalFields() {
            assertThatCode(() -> MealItem.create(
                    "Agua", BigDecimal.valueOf(250), BigDecimal.ZERO,
                    BigDecimal.ZERO, null, null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechaza nombre nulo")
        void rejectsNullName() {
            assertThatThrownBy(() -> MealItem.create(
                    null, BigDecimal.valueOf(100), BigDecimal.valueOf(50),
                    BigDecimal.valueOf(10), null, null, null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("obligatorio");
        }

        @Test
        @DisplayName("rechaza nombre en blanco")
        void rejectsBlankName() {
            assertThatThrownBy(() -> MealItem.create(
                    "   ", BigDecimal.valueOf(100), BigDecimal.valueOf(50),
                    BigDecimal.valueOf(10), null, null, null))
                    .isInstanceOf(InvalidMealEntryException.class);
        }

        @Test
        @DisplayName("rechaza cantidad nula")
        void rejectsNullQuantity() {
            assertThatThrownBy(() -> MealItem.create(
                    "Pan", null, BigDecimal.valueOf(50),
                    BigDecimal.valueOf(10), null, null, null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("0.1 gramos");
        }

        @Test
        @DisplayName("acepta la cantidad mínima exacta (0.1 g)")
        void acceptsExactMinimumQuantity() {
            assertThatCode(() -> MealItem.create(
                    "Sal", BigDecimal.valueOf(0.1), BigDecimal.ZERO,
                    BigDecimal.ZERO, null, null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechaza cantidad por debajo del mínimo")
        void rejectsBelowMinimumQuantity() {
            assertThatThrownBy(() -> MealItem.create(
                    "Sal", BigDecimal.valueOf(0.05), BigDecimal.ZERO,
                    BigDecimal.ZERO, null, null, null))
                    .isInstanceOf(InvalidMealEntryException.class);
        }

        @Test
        @DisplayName("rechaza calorías nulas")
        void rejectsNullCalories() {
            assertThatThrownBy(() -> MealItem.create(
                    "Pan", BigDecimal.valueOf(100), null,
                    BigDecimal.valueOf(10), null, null, null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("calorías");
        }

        @Test
        @DisplayName("rechaza calorías negativas")
        void rejectsNegativeCalories() {
            assertThatThrownBy(() -> MealItem.create(
                    "Pan", BigDecimal.valueOf(100), BigDecimal.valueOf(-1),
                    BigDecimal.valueOf(10), null, null, null))
                    .isInstanceOf(InvalidMealEntryException.class);
        }

        @Test
        @DisplayName("rechaza carbohidratos nulos")
        void rejectsNullCarbohydrates() {
            assertThatThrownBy(() -> MealItem.create(
                    "Pan", BigDecimal.valueOf(100), BigDecimal.valueOf(50),
                    null, null, null, null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("carbohidratos");
        }

        @Test
        @DisplayName("rechaza carbohidratos negativos")
        void rejectsNegativeCarbohydrates() {
            assertThatThrownBy(() -> MealItem.create(
                    "Pan", BigDecimal.valueOf(100), BigDecimal.valueOf(50),
                    BigDecimal.valueOf(-5), null, null, null))
                    .isInstanceOf(InvalidMealEntryException.class);
        }

        @Test
        @DisplayName("rechaza proteínas negativas cuando sí se especifican")
        void rejectsNegativeProteinsWhenProvided() {
            assertThatThrownBy(() -> MealItem.create(
                    "Pan", BigDecimal.valueOf(100), BigDecimal.valueOf(50),
                    BigDecimal.valueOf(10), BigDecimal.valueOf(-1), null, null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("proteínas");
        }

        @Test
        @DisplayName("rechaza grasas negativas cuando sí se especifican")
        void rejectsNegativeFatsWhenProvided() {
            assertThatThrownBy(() -> MealItem.create(
                    "Pan", BigDecimal.valueOf(100), BigDecimal.valueOf(50),
                    BigDecimal.valueOf(10), null, BigDecimal.valueOf(-1), null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("grasas");
        }

        @Test
        @DisplayName("acepta proteínas y grasas en cero")
        void acceptsZeroProteinsAndFats() {
            assertThatCode(() -> MealItem.create(
                    "Pan", BigDecimal.valueOf(100), BigDecimal.valueOf(50),
                    BigDecimal.valueOf(10), BigDecimal.ZERO, BigDecimal.ZERO, null))
                    .doesNotThrowAnyException();
        }
    }
}