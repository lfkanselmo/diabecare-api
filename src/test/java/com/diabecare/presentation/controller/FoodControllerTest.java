package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.SearchFoodUseCase;
import com.diabecare.domain.model.Food;
import com.diabecare.presentation.mapper.FoodPresentationMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FoodController")
class FoodControllerTest {

    @Mock
    private SearchFoodUseCase searchFoodUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FoodController controller = new FoodController(searchFoodUseCase, new FoodPresentationMapperImpl());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested
    @DisplayName("GET /api/v1/foods/search")
    class Search {

        @Test
        @DisplayName("retorna 200 con los resultados de búsqueda mapeados correctamente")
        void returns200WithSearchResultsMappedCorrectly() throws Exception {
            when(searchFoodUseCase.search("man")).thenReturn(List.of(validFood()));

            mockMvc.perform(get("/api/v1/foods/search").param("query", "man"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Manzana"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/foods/category/{category}")
    class FindByCategory {

        @Test
        @DisplayName("retorna 200 con los alimentos de la categoría mapeados correctamente")
        void returns200WithCategoryFoodsMappedCorrectly() throws Exception {
            when(searchFoodUseCase.findByCategory("FRUITS")).thenReturn(List.of(validFood()));

            mockMvc.perform(get("/api/v1/foods/category/{category}", "FRUITS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].category").value("FRUITS"));
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Food validFood() {
        return Food.builder()
                .foodId(UUID.randomUUID())
                .name("Manzana")
                .category("FRUITS")
                .caloriesPer100g(BigDecimal.valueOf(52))
                .carbsPer100g(BigDecimal.valueOf(14))
                .proteinsPer100g(BigDecimal.valueOf(0.3))
                .fatsPer100g(BigDecimal.valueOf(0.2))
                .build();
    }
}