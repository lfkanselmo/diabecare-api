package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.UpdateInsulinProfileUseCase;
import com.diabecare.application.port.in.UpdatePatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.mapper.PatientPresentationMapperImpl;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientController")
class PatientControllerTest {

    @Mock private LoadPatientPort loadPatientPort;
    @Mock private UpdatePatientUseCase updatePatientUseCase;
    @Mock private UpdateInsulinProfileUseCase updateInsulinProfileUseCase;
    @Mock private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        PatientController controller = new PatientController(
                loadPatientPort, updatePatientUseCase, updateInsulinProfileUseCase,
                new PatientPresentationMapperImpl(), currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Nested @DisplayName("GET /api/v1/patients/{patientId}")
    class GetById {
        @Test @DisplayName("retorna 200 con el paciente mapeado correctamente cuando existe")
        void returns200WithPatientMappedCorrectlyWhenExists() throws Exception {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            mockMvc.perform(get("/api/v1/patients/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Ana García"))
                    .andExpect(jsonPath("$.diabetesType").value("TYPE_1"));
        }

        @Test @DisplayName("retorna 404 cuando el paciente no existe")
        void returns404WhenPatientDoesNotExist() throws Exception {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());
            mockMvc.perform(get("/api/v1/patients/{patientId}", patientId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested @DisplayName("PUT /api/v1/patients/{patientId}")
    class Update {
        @Test @DisplayName("retorna 200 con el paciente actualizado mapeado correctamente")
        void returns200WithUpdatedPatientMappedCorrectly() throws Exception {
            when(updatePatientUseCase.execute(any())).thenReturn(validPatient());
            mockMvc.perform(put("/api/v1/patients/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"heightCm\":170,\"targetGlucoseMin\":70,\"targetGlucoseMax\":180,"
                                    + "\"dailyCalorieGoal\":2000,\"activityLevel\":\"MODERATELY_ACTIVE\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Ana García"));
        }

        @Test @DisplayName("retorna 400 cuando la talla está fuera del rango 50-250")
        void returns400WhenHeightOutOfRange() throws Exception {
            mockMvc.perform(put("/api/v1/patients/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"heightCm\":300}"))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(updatePatientUseCase);
        }

        @Test @DisplayName("retorna 400 cuando el objetivo calórico supera el máximo de 5000")
        void returns400WhenDailyCalorieGoalExceedsMaximum() throws Exception {
            mockMvc.perform(put("/api/v1/patients/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"dailyCalorieGoal\":6000}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested @DisplayName("PATCH /api/v1/patients/{patientId}/insulin-profile")
    class UpdateInsulinProfile {
        @Test @DisplayName("retorna 200 con el perfil de insulina actualizado")
        void returns200WithUpdatedInsulinProfile() throws Exception {
            when(updateInsulinProfileUseCase.execute(any())).thenReturn(validPatient());
            mockMvc.perform(patch("/api/v1/patients/{patientId}/insulin-profile", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sensitivityFactor\":50,\"carbRatio\":10,\"targetGlucose\":120}"))
                    .andExpect(status().isOk());
        }
    }

    private Patient validPatient() {
        return Patient.create(UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}