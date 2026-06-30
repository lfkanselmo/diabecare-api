package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.Food;
import com.diabecare.presentation.dto.response.FoodResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FoodPresentationMapper")
class FoodPresentationMapperTest {

    private final FoodPresentationMapper mapper = new FoodPresentationMapperImpl();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("mapea todos los campos correctamente")
        void mapsAllFieldsCorrectly() {
            UUID foodId = UUID.randomUUID();
            Food food = Food.builder()
                    .foodId(foodId)
                    .name("Manzana")
                    .category("FRUITS")
                    .caloriesPer100g(BigDecimal.valueOf(52))
                    .carbsPer100g(BigDecimal.valueOf(14))
                    .proteinsPer100g(BigDecimal.valueOf(0.3))
                    .fatsPer100g(BigDecimal.valueOf(0.2))
                    .fiberPer100g(BigDecimal.valueOf(2.4))
                    .sodiumPer100g(BigDecimal.valueOf(1))
                    .build();

            FoodResponse response = mapper.toResponse(food);

            assertThat(response.foodId()).isEqualTo(foodId);
            assertThat(response.name()).isEqualTo("Manzana");
            assertThat(response.category()).isEqualTo("FRUITS");
            assertThat(response.caloriesPer100g()).isEqualByComparingTo(BigDecimal.valueOf(52));
            assertThat(response.fiberPer100g()).isEqualByComparingTo(BigDecimal.valueOf(2.4));
        }
    }
}