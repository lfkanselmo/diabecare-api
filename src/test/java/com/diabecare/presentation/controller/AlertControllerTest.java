package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetAlertsUseCase;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.domain.model.Alert;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertController")
class AlertControllerTest {

    @Mock
    private GetAlertsUseCase getAlertsUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AlertController controller = new AlertController(getAlertsUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/alerts/{patientId}")
    class GetAlerts {

        @Test
        @DisplayName("retorna 200 con las alertas mapeadas correctamente")
        void returns200WithAlertsMappedCorrectly() throws Exception {
            Alert alert = Alert.builder()
                    .type(Alert.AlertType.GLUCOSE_OUT_OF_RANGE)
                    .severity(Alert.Severity.WARNING)
                    .title("Glucosa fuera de rango")
                    .message("Tu última lectura está fuera del rango objetivo")
                    .build();

            when(getAlertsUseCase.getAlerts(patientId)).thenReturn(List.of(alert));

            mockMvc.perform(get("/api/v1/alerts/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].type").value("GLUCOSE_OUT_OF_RANGE"))
                    .andExpect(jsonPath("$[0].severity").value("WARNING"))
                    .andExpect(jsonPath("$[0].title").value("Glucosa fuera de rango"));
        }

        @Test
        @DisplayName("retorna 200 con un arreglo vacío cuando no hay alertas")
        void returns200WithEmptyArrayWhenNoAlerts() throws Exception {
            when(getAlertsUseCase.getAlerts(patientId)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/alerts/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("retorna 403 cuando el paciente no pertenece al usuario autenticado")
        void returns403WhenPatientDoesNotBelongToAuthenticatedUser() throws Exception {
            doThrow(new UnauthorizedResourceAccessException("sin permiso"))
                    .when(currentUserResolver).verifyOwnsPatient(eq(patientId), any());

            mockMvc.perform(get("/api/v1/alerts/{patientId}", patientId))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(getAlertsUseCase);
        }
    }
}