package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidMealEntryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MealEntry")
class MealEntryTest {

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("crea una comida válida sin items")
        void createsValidMealEntry() {
            MealEntry meal = MealEntry.create(
                    patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(10), "nota");

            assertThat(meal.getMealId()).isNotNull();
            assertThat(meal.getPatientId()).isEqualTo(patientId);
            assertThat(meal.getMealType()).isEqualTo(MealType.BREAKFAST);
            assertThat(meal.getItems()).isEmpty();
        }

        @Test
        @DisplayName("rechaza fecha nula")
        void rejectsNullConsumedAt() {
            assertThatThrownBy(() -> MealEntry.create(
                    patientId, MealType.LUNCH, null, null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("obligatoria");
        }

        @Test
        @DisplayName("rechaza fecha futura")
        void rejectsFutureConsumedAt() {
            assertThatThrownBy(() -> MealEntry.create(
                    patientId, MealType.LUNCH, LocalDateTime.now().plusHours(1), null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("futura");
        }
    }

    @Nested
    @DisplayName("addItem / removeItem")
    class AddRemoveItem {

        @Test
        @DisplayName("agrega un item correctamente")
        void addsItem() {
            MealEntry meal = mealWithoutItems();
            MealItem item = itemWithMacros("Pan", 50, 100, 20, 3, 1);

            meal.addItem(item);

            assertThat(meal.getItems()).hasSize(1).contains(item);
        }

        @Test
        @DisplayName("rechaza agregar un item nulo")
        void rejectsNullItem() {
            MealEntry meal = mealWithoutItems();
            assertThatThrownBy(() -> meal.addItem(null))
                    .isInstanceOf(InvalidMealEntryException.class)
                    .hasMessageContaining("no puede ser nulo");
        }

        @Test
        @DisplayName("elimina un item por su id")
        void removesItemById() {
            MealEntry meal = mealWithoutItems();
            MealItem item = itemWithMacros("Pan", 50, 100, 20, 3, 1);
            meal.addItem(item);

            meal.removeItem(item.getMealItemId());

            assertThat(meal.getItems()).isEmpty();
        }

        @Test
        @DisplayName("eliminar un id inexistente no falla ni modifica la lista")
        void removingUnknownIdDoesNothing() {
            MealEntry meal = mealWithoutItems();
            meal.addItem(itemWithMacros("Pan", 50, 100, 20, 3, 1));

            meal.removeItem(UUID.randomUUID());

            assertThat(meal.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("la lista de items expuesta es inmutable")
        void exposedItemsListIsImmutable() {
            MealEntry meal = mealWithoutItems();
            meal.addItem(itemWithMacros("Pan", 50, 100, 20, 3, 1));

            assertThatThrownBy(() -> meal.getItems().add(itemWithMacros("Otro", 10, 50, 5, 1, 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("totales")
    class Totales {

        @Test
        @DisplayName("suma correctamente calorías y carbohidratos de varios items")
        void sumsCaloriesAndCarbohydrates() {
            MealEntry meal = mealWithoutItems();
            meal.addItem(itemWithMacros("Pan", 50, 100, 20, 3, 1));
            meal.addItem(itemWithMacros("Huevo", 60, 80, 1, 6, 5));

            assertThat(meal.getTotalCalories()).isEqualByComparingTo(BigDecimal.valueOf(180));
            assertThat(meal.getTotalCarbohydrates()).isEqualByComparingTo(BigDecimal.valueOf(21));
        }

        @Test
        @DisplayName("suma correctamente proteínas y grasas cuando todos los items las tienen")
        void sumsProteinsAndFatsWhenAllPresent() {
            MealEntry meal = mealWithoutItems();
            meal.addItem(itemWithMacros("Pan", 50, 100, 20, 3, 1));
            meal.addItem(itemWithMacros("Huevo", 60, 80, 1, 6, 5));

            assertThat(meal.getTotalProteins()).isEqualByComparingTo(BigDecimal.valueOf(9));
            assertThat(meal.getTotalFats()).isEqualByComparingTo(BigDecimal.valueOf(6));
        }

        @Test
        @DisplayName("no lanza NullPointerException cuando algún item no tiene proteínas/grasas")
        void doesNotThrowWhenSomeItemsHaveNullProteinsAndFats() {
            MealEntry meal = mealWithoutItems();
            meal.addItem(itemWithMacros("Pan", 50, 100, 20, 3, 1));
            meal.addItem(MealItem.create("Agua", BigDecimal.valueOf(250),
                    BigDecimal.ZERO, BigDecimal.ZERO, null, null, null));

            assertThatCode(meal::getTotalProteins).doesNotThrowAnyException();
            assertThatCode(meal::getTotalFats).doesNotThrowAnyException();

            assertThat(meal.getTotalProteins()).isEqualByComparingTo(BigDecimal.valueOf(3));
            assertThat(meal.getTotalFats()).isEqualByComparingTo(BigDecimal.valueOf(1));
        }

        @Test
        @DisplayName("retorna cero en todos los totales cuando no hay items")
        void returnsZeroTotalsWhenNoItems() {
            MealEntry meal = mealWithoutItems();

            assertThat(meal.getTotalCalories()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(meal.getTotalCarbohydrates()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(meal.getTotalProteins()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(meal.getTotalFats()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private MealEntry mealWithoutItems() {
        return MealEntry.create(patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5), null);
    }

    private MealItem itemWithMacros(String name, double quantity, double calories,
                                    double carbs, double proteins, double fats) {
        return MealItem.create(name, BigDecimal.valueOf(quantity), BigDecimal.valueOf(calories),
                BigDecimal.valueOf(carbs), BigDecimal.valueOf(proteins), BigDecimal.valueOf(fats), null);
    }
}