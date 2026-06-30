package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;
import com.diabecare.infrastructure.persistence.entity.MealEntryEntity;
import com.diabecare.infrastructure.persistence.entity.MealItemEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MealEntryPersistenceMapper")
class MealEntryPersistenceMapperTest {

    private final MealEntryPersistenceMapper mapper = new MealEntryPersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo el enum mealType a String")
        void mapsAllFieldsConvertingMealTypeEnumToString() {
            UUID patientId = UUID.randomUUID();
            MealEntry entry = MealEntry.create(
                    patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5), "desayuno");

            MealEntryEntity entity = mapper.toEntity(entry);

            assertThat(entity.getId()).isEqualTo(entry.getMealId());
            assertThat(entity.getMealType()).isEqualTo("BREAKFAST");
            assertThat(entity.getNotes()).isEqualTo("desayuno");
        }

        @Test
        @DisplayName("ignora createdAt y updatedAt, y deja items en su valor por defecto (lista vacía)")
        void ignoresCreatedAtUpdatedAtAndItemsDefaultsToEmptyList() {
            MealEntry entry = MealEntry.create(
                    UUID.randomUUID(), MealType.LUNCH, LocalDateTime.now().minusMinutes(5), null);
            entry.addItem(MealItem.create("Pan", BigDecimal.valueOf(50),
                    BigDecimal.valueOf(100), BigDecimal.valueOf(20), null, null, null));

            MealEntryEntity entity = mapper.toEntity(entry);

            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
            // items está marcado como ignore = true; al no asignarse, conserva el valor
            // por defecto declarado en la propia entidad (@Builder.Default = new ArrayList<>()),
            // por lo que NUNCA contendrá los items del dominio, pero tampoco será null.
            assertThat(entity.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo el String a enum mealType")
        void mapsAllFieldsConvertingStringToMealTypeEnum() {
            UUID id = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();

            MealEntryEntity entity = MealEntryEntity.builder()
                    .id(id)
                    .patientId(patientId)
                    .mealType("DINNER")
                    .consumedAt(LocalDateTime.now())
                    .notes("cena ligera")
                    .build();

            MealEntry entry = mapper.toDomain(entity);

            assertThat(entry.getMealId()).isEqualTo(id);
            assertThat(entry.getPatientId()).isEqualTo(patientId);
            assertThat(entry.getMealType()).isEqualTo(MealType.DINNER);
            assertThat(entry.getNotes()).isEqualTo("cena ligera");
        }
    }

    @Nested
    @DisplayName("toItemEntity")
    class ToItemEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando mealItemId a id")
        void mapsAllFieldsRenamingMealItemIdToId() {
            MealItem item = MealItem.create("Manzana", BigDecimal.valueOf(150),
                    BigDecimal.valueOf(80), BigDecimal.valueOf(20),
                    BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.3), "FOOD-001");

            MealItemEntity entity = mapper.toItemEntity(item);

            assertThat(entity.getId()).isEqualTo(item.getMealItemId());
            assertThat(entity.getFoodName()).isEqualTo("Manzana");
            assertThat(entity.getQuantityGrams()).isEqualByComparingTo(BigDecimal.valueOf(150));
            assertThat(entity.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(80));
            assertThat(entity.getFoodCode()).isEqualTo("FOOD-001");
        }

        @Test
        @DisplayName("ignora mealEntry, ya que se asigna al persistir la relación")
        void ignoresMealEntry() {
            MealItem item = MealItem.create("Pan", BigDecimal.valueOf(50),
                    BigDecimal.valueOf(100), BigDecimal.valueOf(20), null, null, null);

            MealItemEntity entity = mapper.toItemEntity(item);

            assertThat(entity.getMealEntry()).isNull();
        }

        @Test
        @DisplayName("mapea correctamente cuando proteínas y grasas son nulas")
        void mapsCorrectlyWhenProteinsAndFatsAreNull() {
            MealItem item = MealItem.create("Agua", BigDecimal.valueOf(250),
                    BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);

            MealItemEntity entity = mapper.toItemEntity(item);

            assertThat(entity.getProteins()).isNull();
            assertThat(entity.getFats()).isNull();
        }
    }

    @Nested
    @DisplayName("toItemDomain")
    class ToItemDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando id a mealItemId")
        void mapsAllFieldsRenamingIdToMealItemId() {
            UUID id = UUID.randomUUID();

            MealItemEntity entity = MealItemEntity.builder()
                    .id(id)
                    .foodName("Arroz")
                    .quantityGrams(BigDecimal.valueOf(200))
                    .calories(BigDecimal.valueOf(260))
                    .carbohydrates(BigDecimal.valueOf(56))
                    .proteins(BigDecimal.valueOf(5))
                    .fats(BigDecimal.valueOf(0.5))
                    .build();

            MealItem item = mapper.toItemDomain(entity);

            assertThat(item.getMealItemId()).isEqualTo(id);
            assertThat(item.getFoodName()).isEqualTo("Arroz");
            assertThat(item.getQuantityGrams()).isEqualByComparingTo(BigDecimal.valueOf(200));
            assertThat(item.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(260));
        }
    }
}