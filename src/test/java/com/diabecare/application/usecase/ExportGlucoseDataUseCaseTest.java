package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.domain.service.GlucoseExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportGlucoseDataUseCaseImpl")
class ExportGlucoseDataUseCaseTest {

    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;

    private ExportGlucoseDataUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();
    private final LocalDateTime from = LocalDateTime.now().minusDays(7);
    private final LocalDateTime to = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        useCase = new ExportGlucoseDataUseCaseImpl(loadGlucoseReadingPort, new GlucoseExportService());
    }

    @Nested
    @DisplayName("exportAsCsv")
    class ExportAsCsv {

        @Test
        @DisplayName("exporta las lecturas del rango indicado en formato CSV")
        void exportsReadingsInRangeAsCsv() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(100));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(List.of(reading));

            String csv = useCase.exportAsCsv(patientId, from, to);

            assertThat(csv).contains("100");
            assertThat(csv).contains(reading.getReadingId().toString());
        }

        @Test
        @DisplayName("retorna solo la cabecera cuando no hay lecturas en el rango")
        void returnsOnlyHeaderWhenNoReadings() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(List.of());

            String csv = useCase.exportAsCsv(patientId, from, to);

            assertThat(csv).contains("id,valor,unidad,tipo,estado,fecha,notas");
        }
    }

    @Nested
    @DisplayName("exportAsJson")
    class ExportAsJson {

        @Test
        @DisplayName("exporta las lecturas del rango indicado en formato JSON")
        void exportsReadingsInRangeAsJson() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(120));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(List.of(reading));

            String json = useCase.exportAsJson(patientId, from, to);

            assertThat(json).contains("120");
        }

        @Test
        @DisplayName("retorna un arreglo vacío cuando no hay lecturas en el rango")
        void returnsEmptyArrayWhenNoReadings() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(List.of());

            String json = useCase.exportAsJson(patientId, from, to);

            assertThat(json.trim()).isEqualTo("[]");
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private GlucoseReading readingWith(BigDecimal value) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(patientId)
                .value(value)
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}