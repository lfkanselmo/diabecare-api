package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.DeactivateMedicationUseCase;
import com.diabecare.application.port.in.GetMedicationsUseCase;
import com.diabecare.application.port.in.RegisterMedicationUseCase;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.mapper.MedicationPresentationMapperImpl;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicationController")
class MedicationControllerTest {

    @Mock
    private RegisterMedicationUseCase registerMedicationUseCase;
    @Mock
    private GetMedicationsUseCase getMedicationsUseCase;
    @Mock
    private DeactivateMedicationUseCase deactivateMedicationUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MedicationController controller = new MedicationController(
                registerMedicationUseCase, getMedicationsUseCase, deactivateMedicationUseCase,
                new MedicationPresentationMapperImpl(), currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/medications/{patientId}")
    class Register {

        @Test
        @DisplayName("retorna 201 con el medicamento registrado correctamente mapeado")
        void returns201WithRegisteredMedicationMappedCorrectly() throws Exception {
            Medication medication = Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                    DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), null);

            when(registerMedicationUseCase.execute(any())).thenReturn(medication);

            String body = """
                    {"name":"Metformina","type":"ORAL","dose":500,"doseUnit":"MG","frequency":"TWICE_DAILY"}
                    """;

            mockMvc.perform(post("/api/v1/medications/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Metformina"));
        }

        @Test
        @DisplayName("retorna 400 cuando la dosis es menor o igual a 0")
        void returns400WhenDoseIsZeroOrNegative() throws Exception {
            String body = """
                    {"name":"Metformina","type":"ORAL","dose":0,"doseUnit":"MG","frequency":"TWICE_DAILY"}
                    """;

            mockMvc.perform(post("/api/v1/medications/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(registerMedicationUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medications/{patientId}")
    class GetActive {

        @Test
        @DisplayName("retorna 200 con los medicamentos activos mapeados correctamente")
        void returns200WithActiveMedicationsMappedCorrectly() throws Exception {
            Medication medication = Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                    DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), null);

            when(getMedicationsUseCase.getActiveByPatientId(patientId)).thenReturn(List.of(medication));

            mockMvc.perform(get("/api/v1/medications/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Metformina"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/medications/{patientId}/{medicationId}")
    class Deactivate {

        @Test
        @DisplayName("retorna 204 al desactivar correctamente")
        void returns204OnSuccessfulDeactivation() throws Exception {
            UUID medicationId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/medications/{patientId}/{medicationId}", patientId, medicationId))
                    .andExpect(status().isNoContent());

            verify(deactivateMedicationUseCase).execute(medicationId, patientId);
        }
    }
}