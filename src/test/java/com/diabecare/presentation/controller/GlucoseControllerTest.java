package com.diabecare.presentation.controller;

import com.diabecare.application.dto.GlucoseStatsRecord;
import com.diabecare.application.port.in.*;
import com.diabecare.domain.model.AgpHourlyBucket;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.mapper.GlucoseReadingPresentationMapperImpl;
import com.diabecare.presentation.mapper.GlucoseStatsPresentationMapperImpl;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlucoseController")
class GlucoseControllerTest {

    @Mock
    private RegisterGlucoseReadingUseCase registerGlucoseReadingUseCase;
    @Mock
    private GetGlucoseHistoryUseCase getGlucoseHistoryUseCase;
    @Mock
    private GetGlucoseStatsUseCase getGlucoseStatsUseCase;
    @Mock
    private GetLatestGlucoseReadingUseCase getLatestGlucoseReadingUseCase;
    @Mock
    private DeleteGlucoseReadingUseCase deleteGlucoseReadingUseCase;
    @Mock
    private GetAgpProfileUseCase getAgpProfileUseCase;
    @Mock
    private ExportGlucoseDataUseCase exportGlucoseDataUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        GlucoseController controller = new GlucoseController(
                registerGlucoseReadingUseCase, getGlucoseHistoryUseCase, getGlucoseStatsUseCase,
                getLatestGlucoseReadingUseCase, deleteGlucoseReadingUseCase, getAgpProfileUseCase,
                new GlucoseReadingPresentationMapperImpl(), new GlucoseStatsPresentationMapperImpl(),
                exportGlucoseDataUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/glucose/{patientId}")
    class Register {

        @Test
        @DisplayName("retorna 201 con la lectura registrada correctamente mapeada")
        void returns201WithRegisteredReadingMappedCorrectly() throws Exception {
            GlucoseReading reading = GlucoseReading.create(
                    patientId, BigDecimal.valueOf(120), GlucoseUnit.MG_DL,
                    ReadingType.FASTING, LocalDateTime.now().minusMinutes(5), null, null);

            when(registerGlucoseReadingUseCase.execute(any())).thenReturn(reading);

            String body = """
                    {"value":120,"unit":"MG_DL","readingType":"FASTING","measuredAt":"2026-06-15T08:00:00"}
                    """;

            mockMvc.perform(post("/api/v1/glucose/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.value").value(120));
        }

        @Test
        @DisplayName("retorna 400 cuando el valor supera el máximo de 600")
        void returns400WhenValueExceedsMaximum() throws Exception {
            String body = """
                    {"value":700,"unit":"MG_DL","readingType":"FASTING","measuredAt":"2026-06-15T08:00:00"}
                    """;

            mockMvc.perform(post("/api/v1/glucose/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(registerGlucoseReadingUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/glucose/{patientId}/history")
    class GetHistory {

        @Test
        @DisplayName("retorna 200 con readings y mealMarkers vacíos cuando no hay datos")
        void returns200WithEmptyReadingsAndMealMarkersWhenNoData() throws Exception {
            when(getGlucoseHistoryUseCase.getByPatientAndDateRange(eq(patientId), any(), any()))
                    .thenReturn(new GetGlucoseHistoryUseCase.Result(List.of(), List.of()));

            mockMvc.perform(get("/api/v1/glucose/{patientId}/history", patientId)
                            .param("from", "2026-06-01T00:00:00")
                            .param("to", "2026-06-07T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.readings").isEmpty())
                    .andExpect(jsonPath("$.mealMarkers").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/glucose/{patientId}/stats")
    class GetStats {

        @Test
        @DisplayName("retorna 200 con las estadísticas mapeadas correctamente")
        void returns200WithStatsMappedCorrectly() throws Exception {
            GlucoseStatsRecord stats = new GlucoseStatsRecord(
                    BigDecimal.valueOf(120), BigDecimal.valueOf(15), BigDecimal.valueOf(12),
                    BigDecimal.valueOf(6.5), BigDecimal.valueOf(75), BigDecimal.valueOf(10),
                    BigDecimal.valueOf(15), 30);

            when(getGlucoseStatsUseCase.getStats(eq(patientId), any(), any())).thenReturn(stats);

            mockMvc.perform(get("/api/v1/glucose/{patientId}/stats", patientId)
                            .param("from", "2026-06-01T00:00:00")
                            .param("to", "2026-06-07T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.average").value(120))
                    .andExpect(jsonPath("$.totalReadings").value(30));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/glucose/{patientId}/latest")
    class GetLatest {

        @Test
        @DisplayName("retorna 200 con la lectura cuando existe")
        void returns200WithReadingWhenExists() throws Exception {
            GlucoseReading reading = GlucoseReading.create(
                    patientId, BigDecimal.valueOf(100), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now().minusMinutes(5), null, null);

            when(getLatestGlucoseReadingUseCase.getLatest(patientId)).thenReturn(Optional.of(reading));

            mockMvc.perform(get("/api/v1/glucose/{patientId}/latest", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.value").value(100));
        }

        @Test
        @DisplayName("retorna 204 sin cuerpo cuando el paciente no tiene lecturas")
        void returns204WithoutBodyWhenNoReadings() throws Exception {
            when(getLatestGlucoseReadingUseCase.getLatest(patientId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/glucose/{patientId}/latest", patientId))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/glucose/{patientId}/agp-profile")
    class GetAgpProfile {

        @Test
        @DisplayName("retorna 200 con los buckets horarios mapeados correctamente")
        void returns200WithHourlyBucketsMappedCorrectly() throws Exception {
            LocalDateTime from = LocalDateTime.now().minusDays(14);
            LocalDateTime to = LocalDateTime.now();

            AgpHourlyBucket bucket = AgpHourlyBucket.builder()
                    .hour(8)
                    .p10(BigDecimal.valueOf(80))
                    .p25(BigDecimal.valueOf(95))
                    .median(BigDecimal.valueOf(110))
                    .p75(BigDecimal.valueOf(130))
                    .p90(BigDecimal.valueOf(150))
                    .readingCount(6)
                    .build();

            when(getAgpProfileUseCase.execute(patientId, from, to)).thenReturn(List.of(bucket));

            mockMvc.perform(get("/api/v1/glucose/{patientId}/agp-profile", patientId)
                            .param("from", from.toString())
                            .param("to", to.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].hour").value(8))
                    .andExpect(jsonPath("$[0].median").value(110))
                    .andExpect(jsonPath("$[0].readingCount").value(6));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/glucose/{patientId}/{readingId}")
    class Delete {

        @Test
        @DisplayName("retorna 204 al eliminar correctamente")
        void returns204OnSuccessfulDeletion() throws Exception {
            UUID readingId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/glucose/{patientId}/{readingId}", patientId, readingId))
                    .andExpect(status().isNoContent());

            verify(deleteGlucoseReadingUseCase).execute(readingId, patientId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/glucose/{patientId}/export/csv")
    class ExportCsv {

        @Test
        @DisplayName("retorna 200 con el header Content-Disposition de descarga")
        void returns200WithContentDispositionHeader() throws Exception {
            when(exportGlucoseDataUseCase.exportAsCsv(eq(patientId), any(), any()))
                    .thenReturn("id,valor\n");

            mockMvc.perform(get("/api/v1/glucose/{patientId}/export/csv", patientId)
                            .param("from", "2026-06-01T00:00:00")
                            .param("to", "2026-06-07T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"glucosa.csv\""));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/glucose/{patientId}/export/json")
    class ExportJson {

        @Test
        @DisplayName("retorna 200 con el header Content-Disposition de descarga")
        void returns200WithContentDispositionHeader() throws Exception {
            when(exportGlucoseDataUseCase.exportAsJson(eq(patientId), any(), any()))
                    .thenReturn("[]");

            mockMvc.perform(get("/api/v1/glucose/{patientId}/export/json", patientId)
                            .param("from", "2026-06-01T00:00:00")
                            .param("to", "2026-06-07T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"glucosa.json\""));
        }
    }
}