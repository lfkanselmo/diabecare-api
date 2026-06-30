package com.diabecare.presentation.controller;

import com.diabecare.domain.model.*;
import com.diabecare.domain.service.CycleLabelService;
import com.diabecare.domain.service.ExerciseLabelService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetadataController")
class MetadataControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MetadataController controller = new MetadataController(
                new CycleLabelService((key, args) -> "etiqueta"),
                new MenstrualCycleGuidanceService((key, args) -> "etiqueta"),
                new ExerciseLabelService());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test @DisplayName("exercise-types retorna todos los tipos de ejercicio")
    void exerciseTypesReturnsAllValues() throws Exception {
        mockMvc.perform(get("/api/v1/metadata/exercise-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(ExerciseType.values().length));
    }

    @Test @DisplayName("medication-types retorna las etiquetas correctas")
    void medicationTypesReturnsCorrectLabels() throws Exception {
        mockMvc.perform(get("/api/v1/metadata/medication-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(MedicationType.values().length))
                .andExpect(jsonPath("$[?(@.value == 'ORAL')].label").value("Medicamento oral"));
    }

    @Test @DisplayName("dose-units retorna las 3 unidades con sus etiquetas")
    void doseUnitsReturnsThreeUnitsWithLabels() throws Exception {
        mockMvc.perform(get("/api/v1/metadata/dose-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.value == 'MG')].label").value("mg"));
    }

    @Test @DisplayName("meal-types retorna las 4 comidas con sus etiquetas")
    void mealTypesReturnsFourMealsWithLabels() throws Exception {
        mockMvc.perform(get("/api/v1/metadata/meal-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[?(@.value == 'BREAKFAST')].label").value("Desayuno"));
    }

    @Test @DisplayName("glucose-statuses retorna los 5 estados con sus etiquetas")
    void glucoseStatusesReturnsFiveStatusesWithLabels() throws Exception {
        mockMvc.perform(get("/api/v1/metadata/glucose-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[?(@.value == 'CRITICALLY_LOW')].label").value("Crítico bajo"));
    }

    @Test @DisplayName("diabetes-types retorna los 5 tipos con sus etiquetas")
    void diabetesTypesReturnsFiveTypesWithLabels() throws Exception {
        mockMvc.perform(get("/api/v1/metadata/diabetes-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test @DisplayName("cycle-symptoms delega correctamente al servicio de etiquetas de ciclo")
    void cycleSymptomsDelegatesToCycleLabelService() throws Exception {
        mockMvc.perform(get("/api/v1/metadata/cycle-symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(CycleSymptom.values().length));
    }

    @Test @DisplayName("cycle-phases delega correctamente al servicio de guía del ciclo")
    void cyclePhasesDelegatesToCycleGuidanceService() throws Exception {
        mockMvc.perform(get("/api/v1/metadata/cycle-phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(CyclePhase.values().length));
    }
}