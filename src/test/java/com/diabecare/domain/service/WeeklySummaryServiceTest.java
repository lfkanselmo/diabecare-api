package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklySummaryService")
class WeeklySummaryServiceTest {

    @Mock
    private MessageResolverPort messages;

    private WeeklySummaryService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new WeeklySummaryService(new MedicalCalculatorService(), messages);
    }

    @Nested
    @DisplayName("buildSummary")
    class BuildSummary {

        @Test
        @DisplayName("retorna empty cuando no hay lecturas")
        void returnsEmptyWhenNoReadings() {
            Patient patient = validPatient();
            assertThat(service.buildSummary(patient, List.of())).isEmpty();
        }

        @Test
        @DisplayName("construye el resumen con los datos correctos del paciente")
        void buildsSummaryWithCorrectPatientData() {
            Patient patient = validPatient();
            List<GlucoseReading> readings = readingsWith(100, 110, 90);

            Optional<WeeklySummaryData> result = service.buildSummary(patient, readings);

            assertThat(result).isPresent();
            assertThat(result.get().patientId()).isEqualTo(patient.getPatientId());
            assertThat(result.get().patientName()).isEqualTo(patient.getFullName());
            assertThat(result.get().totalReadings()).isEqualTo(3);
        }

        @Test
        @DisplayName("cuenta correctamente los episodios de hipoglucemia (menores a 70)")
        void countsHypoglycemiaEpisodesCorrectly() {
            Patient patient = validPatient(); // rango 70-180

            // 65 y 60 son hipoglucemia (< 70); 100 y 70 exacto no cuentan
            List<GlucoseReading> readings = readingsWith(65, 60, 100, 70);

            Optional<WeeklySummaryData> result = service.buildSummary(patient, readings);

            assertThat(result.get().hypoEpisodes()).isEqualTo(2);
        }

        @Test
        @DisplayName("cuenta correctamente los episodios de hiperglucemia (mayores al máximo del paciente)")
        void countsHyperglycemiaEpisodesCorrectly() {
            Patient patient = validPatient(); // targetGlucoseMax = 180 por defecto

            // 200 y 250 superan 180; 180 exacto no cuenta
            List<GlucoseReading> readings = readingsWith(200, 250, 100, 180);

            Optional<WeeklySummaryData> result = service.buildSummary(patient, readings);

            assertThat(result.get().hyperEpisodes()).isEqualTo(2);
        }

        @Test
        @DisplayName("calcula el promedio de glucosa correctamente")
        void calculatesAverageGlucoseCorrectly() {
            Patient patient = validPatient();
            List<GlucoseReading> readings = readingsWith(100, 120, 140);

            Optional<WeeklySummaryData> result = service.buildSummary(patient, readings);

            assertThat(result.get().averageGlucose()).isEqualByComparingTo(BigDecimal.valueOf(120));
        }
    }

    @Nested
    @DisplayName("buildPushTitle")
    class BuildPushTitle {

        @Test
        @DisplayName("resuelve la clave correcta para el título")
        void resolvesCorrectKey() {
            when(messages.resolve("weekly-summary.push.title")).thenReturn("Tu resumen semanal");

            String title = service.buildPushTitle();

            assertThat(title).isEqualTo("Tu resumen semanal");
            verify(messages).resolve("weekly-summary.push.title");
        }
    }

    @Nested
    @DisplayName("buildPushMessage")
    class BuildPushMessage {

        @Test
        @DisplayName("resuelve la clave correcta pasando los datos del resumen como argumentos")
        void resolvesCorrectKeyWithSummaryData() {
            WeeklySummaryData data = new WeeklySummaryData(
                    userId, "Ana", BigDecimal.valueOf(120), BigDecimal.valueOf(6.0),
                    BigDecimal.valueOf(80), 2L, 1L, 20);

            when(messages.resolve(eq("weekly-summary.push.message"), any(), any(), any(), any(), any()))
                    .thenReturn("mensaje");

            String message = service.buildPushMessage(data);

            assertThat(message).isEqualTo("mensaje");
            verify(messages).resolve("weekly-summary.push.message",
                    data.averageGlucose(), data.timeInRangePercent(), data.estimatedHba1c(),
                    data.hypoEpisodes(), data.hyperEpisodes());
        }
    }


    private Patient validPatient() {
        return Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }

    private List<GlucoseReading> readingsWith(int... values) {
        return java.util.Arrays.stream(values)
                .mapToObj(v -> GlucoseReading.builder()
                        .readingId(UUID.randomUUID())
                        .patientId(userId)
                        .value(BigDecimal.valueOf(v))
                        .unit(GlucoseUnit.MG_DL)
                        .readingType(ReadingType.RANDOM)
                        .measuredAt(LocalDateTime.now())
                        .build())
                .toList();
    }
}
