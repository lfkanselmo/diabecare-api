package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.Food;
import com.diabecare.infrastructure.persistence.entity.FoodEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FoodPersistenceMapper")
class FoodPersistenceMapperTest {

    private final FoodPersistenceMapper mapper = new FoodPersistenceMapperImpl();

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando id a foodId")
        void mapsAllFieldsRenamingIdToFoodId() {
            UUID id = UUID.randomUUID();
            FoodEntity entity = FoodEntity.builder()
                    .id(id)
                    .name("Manzana")
                    .category("FRUITS")
                    .caloriesPer100g(BigDecimal.valueOf(52))
                    .carbsPer100g(BigDecimal.valueOf(14))
                    .proteinsPer100g(BigDecimal.valueOf(0.3))
                    .fatsPer100g(BigDecimal.valueOf(0.2))
                    .fiberPer100g(BigDecimal.valueOf(2.4))
                    .sodiumPer100g(BigDecimal.valueOf(1))
                    .createdAt(LocalDateTime.now())
                    .build();

            Food food = mapper.toDomain(entity);

            assertThat(food.getFoodId()).isEqualTo(id);
            assertThat(food.getName()).isEqualTo("Manzana");
            assertThat(food.getCategory()).isEqualTo("FRUITS");
            assertThat(food.getCaloriesPer100g()).isEqualByComparingTo(BigDecimal.valueOf(52));
            assertThat(food.getCarbsPer100g()).isEqualByComparingTo(BigDecimal.valueOf(14));
            assertThat(food.getProteinsPer100g()).isEqualByComparingTo(BigDecimal.valueOf(0.3));
            assertThat(food.getFatsPer100g()).isEqualByComparingTo(BigDecimal.valueOf(0.2));
        }

        @Test
        @DisplayName("mapea correctamente cuando los campos opcionales (fibra, sodio) son nulos")
        void mapsCorrectlyWhenOptionalFieldsAreNull() {
            FoodEntity entity = FoodEntity.builder()
                    .id(UUID.randomUUID())
                    .name("Agua")
                    .category("BEVERAGES")
                    .caloriesPer100g(BigDecimal.ZERO)
                    .carbsPer100g(BigDecimal.ZERO)
                    .proteinsPer100g(BigDecimal.ZERO)
                    .fatsPer100g(BigDecimal.ZERO)
                    .fiberPer100g(null)
                    .sodiumPer100g(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            Food food = mapper.toDomain(entity);

            assertThat(food.getFiberPer100g()).isNull();
            assertThat(food.getSodiumPer100g()).isNull();
        }
    }
}