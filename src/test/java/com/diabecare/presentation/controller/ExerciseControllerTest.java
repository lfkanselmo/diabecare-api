package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetExerciseHistoryUseCase;
import com.diabecare.application.port.in.RegisterExerciseUseCase;
import com.diabecare.application.port.in.SyncExerciseLogsUseCase;
import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseController")
class ExerciseControllerTest {

    @Mock
    private RegisterExerciseUseCase registerExerciseUseCase;
    @Mock
    private GetExerciseHistoryUseCase getExerciseHistoryUseCase;
    @Mock
    private SyncExerciseLogsUseCase syncExerciseLogsUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ExerciseController controller = new ExerciseController(
                registerExerciseUseCase, getExerciseHistoryUseCase, syncExerciseLogsUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/exercise/{patientId}")
    class Register {

        @Test
        @DisplayName("retorna 201 con el ejercicio registrado correctamente mapeado")
        void returns201WithRegisteredExerciseMappedCorrectly() throws Exception {
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.RUNNING, ExerciseIntensity.HIGH,
                    45, "carrera", LocalDateTime.now().minusMinutes(5), BigDecimal.valueOf(400));

            when(registerExerciseUseCase.execute(any())).thenReturn(log);

            String body = """
                    {"exerciseType":"RUNNING","intensity":"HIGH","durationMinutes":45,
                     "notes":"carrera","caloriesBurned":400}
                    """;

            mockMvc.perform(post("/api/v1/exercise/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.exerciseType").value("RUNNING"))
                    .andExpect(jsonPath("$.intensity").value("HIGH"));
        }

        @Test
        @DisplayName("retorna 400 cuando durationMinutes es menor a 1")
        void returns400WhenDurationMinutesBelowOne() throws Exception {
            String body = """
                    {"exerciseType":"RUNNING","intensity":"HIGH","durationMinutes":0}
                    """;

            mockMvc.perform(post("/api/v1/exercise/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(registerExerciseUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/exercise/{patientId}/history")
    class GetHistory {

        @Test
        @DisplayName("retorna 200 con el historial mapeado correctamente")
        void returns200WithHistoryMappedCorrectly() throws Exception {
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.LOW,
                    30, null, LocalDateTime.now().minusDays(1), BigDecimal.valueOf(100));

            var pageable = PageRequest.of(0, 20);
            when(getExerciseHistoryUseCase.getHistory(eq(patientId), any(), any(), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(log), pageable, 1));

            mockMvc.perform(get("/api/v1/exercise/{patientId}/history", patientId)
                            .param("from", "2026-06-01T00:00:00")
                            .param("to", "2026-06-07T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].exerciseType").value("WALKING"));
        }

        @Test
        @DisplayName("retorna 403 cuando el paciente no pertenece al usuario autenticado")
        void returns403WhenPatientDoesNotBelongToUser() throws Exception {
            doThrow(new com.diabecare.domain.exception.UnauthorizedResourceAccessException("sin permiso"))
                    .when(currentUserResolver).verifyOwnsPatient(eq(patientId), any());

            mockMvc.perform(get("/api/v1/exercise/{patientId}/history", patientId)
                            .param("from", "2026-06-01T00:00:00")
                            .param("to", "2026-06-07T23:59:59"))
                    .andExpect(status().isForbidden());
        }
    }
}