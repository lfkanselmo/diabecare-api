package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;
import com.diabecare.presentation.dto.response.MealEntryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MealEntryPresentationMapper")
class MealEntryPresentationMapperTest {

    private final MealEntryPresentationMapper mapper = new MealEntryPresentationMapperImpl();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("calcula los 4 totales nutricionales sumando los items, y convierte mealType a String")
        void calculatesFourNutritionalTotalsAndConvertsMealTypeToString() {
            UUID patientId = UUID.randomUUID();
            MealEntry entry = MealEntry.create(
                    patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5), "desayuno");
            entry.addItem(MealItem.create("Pan", BigDecimal.valueOf(50),
                    BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(5), BigDecimal.valueOf(2), null));
            entry.addItem(MealItem.create("Huevo", BigDecimal.valueOf(60),
                    BigDecimal.valueOf(80), BigDecimal.valueOf(1), BigDecimal.valueOf(6), BigDecimal.valueOf(5), null));

            MealEntryResponse response = mapper.toResponse(entry);

            assertThat(response.mealType()).isEqualTo("BREAKFAST");
            assertThat(response.totalCalories()).isEqualByComparingTo(BigDecimal.valueOf(180));
            assertThat(response.totalCarbohydrates()).isEqualByComparingTo(BigDecimal.valueOf(21));
            assertThat(response.totalProteins()).isEqualByComparingTo(BigDecimal.valueOf(11));
            assertThat(response.totalFats()).isEqualByComparingTo(BigDecimal.valueOf(7));
        }

        @Test
        @DisplayName("calcula totales en cero cuando la comida no tiene items")
        void calculatesZeroTotalsWhenMealHasNoItems() {
            MealEntry entry = MealEntry.create(
                    UUID.randomUUID(), MealType.SNACK, LocalDateTime.now().minusMinutes(5), null);

            MealEntryResponse response = mapper.toResponse(entry);

            assertThat(response.totalCalories()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("toItemResponse")
    class ToItemResponse {

        @Test
        @DisplayName("mapea un item individual correctamente")
        void mapsIndividualItemCorrectly() {
            MealItem item = MealItem.create("Manzana", BigDecimal.valueOf(150),
                    BigDecimal.valueOf(80), BigDecimal.valueOf(20),
                    BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.3), "FOOD-001");

            MealEntryResponse.MealItemResponse response = mapper.toItemResponse(item);

            assertThat(response.foodName()).isEqualTo("Manzana");
            assertThat(response.quantityGrams()).isEqualByComparingTo(BigDecimal.valueOf(150));
            assertThat(response.calories()).isEqualByComparingTo(BigDecimal.valueOf(80));
        }
    }
}