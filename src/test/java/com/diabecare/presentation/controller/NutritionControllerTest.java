package com.diabecare.presentation.controller;

import com.diabecare.application.dto.DailySummaryRecord;
import com.diabecare.application.port.in.GetDailySummaryUseCase;
import com.diabecare.application.port.in.GetMealHistoryUseCase;
import com.diabecare.application.port.in.RegisterMealEntryUseCase;
import com.diabecare.application.port.in.SyncMealEntriesUseCase;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealType;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.mapper.DailySummaryPresentationMapperImpl;
import com.diabecare.presentation.mapper.MealEntryPresentationMapperImpl;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NutritionController")
class NutritionControllerTest {

    @Mock private RegisterMealEntryUseCase registerMealEntryUseCase;
    @Mock private GetDailySummaryUseCase getDailySummaryUseCase;
    @Mock private GetMealHistoryUseCase getMealHistoryUseCase;
    @Mock private SyncMealEntriesUseCase syncMealEntriesUseCase;
    @Mock private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        NutritionController controller = new NutritionController(
                registerMealEntryUseCase, getDailySummaryUseCase, getMealHistoryUseCase,
                syncMealEntriesUseCase, new MealEntryPresentationMapperImpl(),
                new DailySummaryPresentationMapperImpl(), currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Nested @DisplayName("POST /api/v1/nutrition/{patientId}/meals")
    class Register {
        @Test @DisplayName("retorna 201 con la comida registrada correctamente mapeada")
        void returns201WithRegisteredMealMappedCorrectly() throws Exception {
            MealEntry entry = MealEntry.create(patientId, MealType.BREAKFAST,
                    LocalDateTime.now().minusMinutes(5), "desayuno");
            when(registerMealEntryUseCase.execute(any())).thenReturn(entry);
            mockMvc.perform(post("/api/v1/nutrition/{patientId}/meals", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mealType\":\"BREAKFAST\",\"consumedAt\":\"2026-06-15T08:00:00\","
                                    + "\"notes\":\"desayuno\",\"items\":[{\"foodName\":\"Pan\","
                                    + "\"quantityGrams\":50,\"calories\":100,\"carbohydrates\":20}]}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.mealType").value("BREAKFAST"));
        }

        @Test @DisplayName("retorna 400 cuando la lista de items está vacía")
        void returns400WhenItemsListIsEmpty() throws Exception {
            mockMvc.perform(post("/api/v1/nutrition/{patientId}/meals", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mealType\":\"BREAKFAST\",\"consumedAt\":\"2026-06-15T08:00:00\",\"items\":[]}"))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(registerMealEntryUseCase);
        }

        @Test @DisplayName("retorna 400 cuando la cantidad de un item es menor a 0.1")
        void returns400WhenItemQuantityBelowMinimum() throws Exception {
            mockMvc.perform(post("/api/v1/nutrition/{patientId}/meals", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mealType\":\"BREAKFAST\",\"consumedAt\":\"2026-06-15T08:00:00\","
                                    + "\"items\":[{\"foodName\":\"Pan\",\"quantityGrams\":0,"
                                    + "\"calories\":100,\"carbohydrates\":20}]}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested @DisplayName("GET /api/v1/nutrition/{patientId}/summary")
    class GetDailySummary {
        @Test @DisplayName("retorna 200 con el resumen diario mapeado correctamente")
        void returns200WithDailySummaryMappedCorrectly() throws Exception {
            DailySummaryRecord record = new DailySummaryRecord(LocalDate.of(2026, 6, 15),
                    BigDecimal.valueOf(1800), BigDecimal.valueOf(200),
                    BigDecimal.valueOf(80), BigDecimal.valueOf(60), 2000, false);
            when(getDailySummaryUseCase.getSummary(patientId, LocalDate.of(2026, 6, 15))).thenReturn(record);
            mockMvc.perform(get("/api/v1/nutrition/{patientId}/summary", patientId).param("date", "2026-06-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCalories").value(1800));
        }
    }

    @Nested @DisplayName("GET /api/v1/nutrition/{patientId}/meals")
    class GetHistory {
        @Test @DisplayName("retorna 200 con el historial mapeado correctamente")
        void returns200WithMealHistoryMappedCorrectly() throws Exception {
            MealEntry entry = MealEntry.create(patientId, MealType.LUNCH,
                    LocalDateTime.of(2026, 6, 15, 13, 0), null);
            var pageable = PageRequest.of(0, 20);
            when(getMealHistoryUseCase.getHistory(patientId,
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 15), pageable))
                    .thenReturn(new PageImpl<>(List.of(entry), pageable, 1));
            mockMvc.perform(get("/api/v1/nutrition/{patientId}/meals", patientId)
                            .param("from", "2026-06-01").param("to", "2026-06-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].mealType").value("LUNCH"));
        }
    }
}