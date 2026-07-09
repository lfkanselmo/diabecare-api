package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.LookupFoodByBarcodeUseCase;
import com.diabecare.domain.model.ExternalFoodInfo;
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
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("FoodLookupController")
class FoodLookupControllerTest {

    @Mock
    private LookupFoodByBarcodeUseCase lookupFoodByBarcodeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FoodLookupController controller = new FoodLookupController(lookupFoodByBarcodeUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested @DisplayName("GET /api/v1/food-lookup/barcode/{barcode}")
    class LookupByBarcode {

        @Test @DisplayName("retorna 200 con el producto mapeado correctamente cuando existe")
        void returns200WithMappedProductWhenExists() throws Exception {
            ExternalFoodInfo info = ExternalFoodInfo.builder()
                    .barcode("3017620422003").name("Nutella").brand("Ferrero")
                    .caloriesPer100g(BigDecimal.valueOf(539)).build();
            when(lookupFoodByBarcodeUseCase.execute("3017620422003")).thenReturn(Optional.of(info));

            mockMvc.perform(get("/api/v1/food-lookup/barcode/{barcode}", "3017620422003"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Nutella"))
                    .andExpect(jsonPath("$.brand").value("Ferrero"));
        }

        @Test @DisplayName("retorna 404 cuando el producto no existe")
        void returns404WhenProductDoesNotExist() throws Exception {
            when(lookupFoodByBarcodeUseCase.execute("000000000000")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/food-lookup/barcode/{barcode}", "000000000000"))
                    .andExpect(status().isNotFound());
        }
    }
}
