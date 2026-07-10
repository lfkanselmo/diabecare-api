package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.Food;
import com.diabecare.infrastructure.persistence.entity.FoodEntity;
import com.diabecare.infrastructure.persistence.mapper.FoodPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.FoodJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FoodPersistenceAdapter")
class FoodPersistenceAdapterTest {

    @Mock
    private FoodJpaRepository repository;

    private FoodPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FoodPersistenceAdapter(repository, new FoodPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("searchByName")
    class SearchByName {

        @Test
        @DisplayName("busca con un límite de 10 resultados en la primera página")
        void searchesWithLimitOfTenResultsOnFirstPage() {
            when(repository.searchByName(eq("man"), any())).thenReturn(List.of(validFoodEntity()));

            adapter.searchByName("man");

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            verify(repository).searchByName(eq("man"), captor.capture());

            assertThat(captor.getValue().getPageSize()).isEqualTo(10);
            assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("retorna los resultados convertidos a dominio")
        void returnsResultsConvertedToDomain() {
            when(repository.searchByName(eq("man"), any())).thenReturn(List.of(validFoodEntity()));

            List<Food> result = adapter.searchByName("man");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Manzana");
        }
    }

    @Nested
    @DisplayName("findByCategory")
    class FindByCategory {

        @Test
        @DisplayName("retorna los alimentos de la categoría convertidos a dominio")
        void returnsCategoryFoodsConvertedToDomain() {
            when(repository.findByCategoryOrderByName("FRUITS")).thenReturn(List.of(validFoodEntity()));

            List<Food> result = adapter.findByCategory("FRUITS");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("FRUITS");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna el alimento convertido a dominio cuando existe")
        void returnsFoodConvertedToDomainWhenExists() {
            FoodEntity entity = validFoodEntity();
            when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

            Optional<Food> result = adapter.findById(entity.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Manzana");
        }

        @Test
        @DisplayName("retorna Optional vacío cuando no existe")
        void returnsEmptyWhenNotExists() {
            UUID foodId = UUID.randomUUID();
            when(repository.findById(foodId)).thenReturn(Optional.empty());

            Optional<Food> result = adapter.findById(foodId);

            assertThat(result).isEmpty();
        }
    }


    private FoodEntity validFoodEntity() {
        return FoodEntity.builder()
                .id(UUID.randomUUID())
                .name("Manzana")
                .category("FRUITS")
                .caloriesPer100g(BigDecimal.valueOf(52))
                .carbsPer100g(BigDecimal.valueOf(14))
                .proteinsPer100g(BigDecimal.valueOf(0.3))
                .fatsPer100g(BigDecimal.valueOf(0.2))
                .build();
    }
}
