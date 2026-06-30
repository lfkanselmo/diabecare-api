package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.*;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.CycleLabelService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenstrualCycleController")
class MenstrualCycleControllerTest {

    @Mock
    private RegisterMenstrualCycleUseCase registerUseCase;
    @Mock
    private RegisterCycleDayEntryUseCase registerDayEntryUseCase;
    @Mock
    private FinishPeriodUseCase finishPeriodUseCase;
    @Mock
    private GetMenstrualCycleStatusUseCase getStatusUseCase;
    @Mock
    private GetCyclePhaseCalendarUseCase getCyclePhaseCalendarUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MenstrualCycleController controller = new MenstrualCycleController(
                registerUseCase, registerDayEntryUseCase, finishPeriodUseCase,
                getStatusUseCase, getCyclePhaseCalendarUseCase, currentUserResolver,
                new MenstrualCycleGuidanceService((key, args) -> "guía de prueba"),
                new CycleLabelService((key, args) -> "etiqueta de prueba"));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/menstrual-cycle/{patientId}")
    class Register {

        @Test
        @DisplayName("retorna 201 con el estado del ciclo construido correctamente")
        void returns201WithCycleStatusBuiltCorrectly() throws Exception {
            when(getStatusUseCase.getStatus(patientId)).thenReturn(validCycleStatus());

            String body = """
                    {"startDate":"2026-06-01","notes":"ciclo regular"}
                    """;

            mockMvc.perform(post("/api/v1/menstrual-cycle/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.currentPhase").value("MENSTRUATION"))
                    .andExpect(jsonPath("$.isOngoing").value(true));

            verify(registerUseCase).execute(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/menstrual-cycle/{patientId}/finish-period")
    class FinishPeriod {

        @Test
        @DisplayName("retorna 200 con el estado del ciclo actualizado")
        void returns200WithUpdatedCycleStatus() throws Exception {
            when(getStatusUseCase.getStatus(patientId)).thenReturn(validCycleStatus());

            String body = """
                    {"endDate":"2026-06-06"}
                    """;

            mockMvc.perform(post("/api/v1/menstrual-cycle/{patientId}/finish-period", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(finishPeriodUseCase).execute(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/menstrual-cycle/{patientId}/days")
    class RegisterDayEntry {

        @Test
        @DisplayName("retorna 201 con la entrada del día correctamente mapeada, incluyendo síntomas")
        void returns201WithDayEntryMappedCorrectlyIncludingSymptoms() throws Exception {
            CycleDayEntry entry = CycleDayEntry.create(
                    UUID.randomUUID(), patientId, LocalDate.of(2026, 6, 1),
                    FlowIntensity.MODERATE, "nota",
                    List.of(CycleSymptomEntry.builder()
                            .symptom(CycleSymptom.CRAMPS)
                            .severity(SymptomSeverity.MODERATE)
                            .build()));

            when(registerDayEntryUseCase.execute(any())).thenReturn(entry);

            String body = """
                    {"entryDate":"2026-06-01","flowIntensity":"MODERATE","notes":"nota",
                     "symptoms":[{"symptom":"CRAMPS","severity":"MODERATE"}]}
                    """;

            mockMvc.perform(post("/api/v1/menstrual-cycle/{patientId}/days", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.flowIntensity").value("MODERATE"))
                    .andExpect(jsonPath("$.symptoms[0].symptom").value("CRAMPS"));
        }

        @Test
        @DisplayName("retorna 201 correctamente cuando no se envían síntomas")
        void returns201CorrectlyWhenNoSymptomsSent() throws Exception {
            CycleDayEntry entry = CycleDayEntry.create(
                    UUID.randomUUID(), patientId, LocalDate.of(2026, 6, 1),
                    FlowIntensity.LIGHT, null, null);

            when(registerDayEntryUseCase.execute(any())).thenReturn(entry);

            String body = """
                    {"entryDate":"2026-06-01","flowIntensity":"LIGHT"}
                    """;

            mockMvc.perform(post("/api/v1/menstrual-cycle/{patientId}/days", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.symptoms").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/menstrual-cycle/{patientId}/status")
    class GetStatus {

        @Test
        @DisplayName("retorna 200 con el estado del ciclo correctamente construido")
        void returns200WithCycleStatusCorrectlyBuilt() throws Exception {
            when(getStatusUseCase.getStatus(patientId)).thenReturn(validCycleStatus());

            mockMvc.perform(get("/api/v1/menstrual-cycle/{patientId}/status", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dayOfCycle").value(3))
                    .andExpect(jsonPath("$.isOpenTooLong").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/menstrual-cycle/{patientId}/phase-calendar")
    class GetPhaseCalendar {

        @Test
        @DisplayName("retorna 200 con el calendario de fases mapeado correctamente")
        void returns200WithPhaseCalendarMappedCorrectly() throws Exception {
            var dayPhase = new GetCyclePhaseCalendarUseCase.DayPhase(
                    LocalDate.of(2026, 6, 1), CyclePhase.MENSTRUATION);

            when(getCyclePhaseCalendarUseCase.getCalendar(eq(patientId), any(), any()))
                    .thenReturn(List.of(dayPhase));

            mockMvc.perform(get("/api/v1/menstrual-cycle/{patientId}/phase-calendar", patientId)
                            .param("from", "2026-06-01")
                            .param("to", "2026-06-07"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].phase").value("MENSTRUATION"));
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private GetMenstrualCycleStatusUseCase.CycleStatus validCycleStatus() {
        return new GetMenstrualCycleStatusUseCase.CycleStatus(
                CyclePhase.MENSTRUATION, 3, true, false, false,
                LocalDate.of(2026, 6, 1), LocalDate.now().plusDays(25),
                "guía de glucosa", 28, 5, null, List.of());
    }
}