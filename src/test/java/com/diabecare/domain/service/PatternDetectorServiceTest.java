package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.model.Alert;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatternDetectorService")
class PatternDetectorServiceTest {

    @Mock
    private SystemConfigPort systemConfig;

    @Mock
    private MessageResolverPort messages;

    private PatternDetectorService service;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PatternDetectorService(new MedicalCalculatorService(), systemConfig, messages);
        lenient().when(messages.resolve(any(), any())).thenReturn("mensaje");
        lenient().when(messages.resolve(any())).thenReturn("título");
    }

    @Nested
    @DisplayName("detectHighFastingPattern")
    class DetectHighFastingPattern {

        @Test
        @DisplayName("detecta el patrón cuando el 60% o más de los ayunos superan el umbral")
        void detectsPatternWhenRatioMet() {
            when(systemConfig.getInt("pattern.fasting_threshold_mgdl")).thenReturn(130);
            when(systemConfig.getDecimal("pattern.fasting_ratio")).thenReturn(0.6);
            when(systemConfig.getInt("alert.min_readings_for_stats")).thenReturn(3);

            // 3 de 5 por encima de 130 = 60%, justo en el umbral
            List<GlucoseReading> readings = readingsOfType(ReadingType.FASTING, 140, 135, 150, 100, 110);

            Optional<Alert> alert = service.detectHighFastingPattern(readings);

            assertThat(alert).isPresent();
            assertThat(alert.get().getType()).isEqualTo(Alert.AlertType.GLUCOSE_PATTERN_DETECTED);
            assertThat(alert.get().getSeverity()).isEqualTo(Alert.Severity.WARNING);
        }

        @Test
        @DisplayName("no detecta el patrón cuando el ratio está por debajo del umbral")
        void doesNotDetectWhenRatioBelowThreshold() {
            when(systemConfig.getInt("pattern.fasting_threshold_mgdl")).thenReturn(130);
            when(systemConfig.getDecimal("pattern.fasting_ratio")).thenReturn(0.6);
            when(systemConfig.getInt("alert.min_readings_for_stats")).thenReturn(3);

            // 2 de 5 por encima de 130 = 40%, por debajo del 60%
            List<GlucoseReading> readings = readingsOfType(ReadingType.FASTING, 140, 135, 100, 110, 105);

            Optional<Alert> alert = service.detectHighFastingPattern(readings);

            assertThat(alert).isEmpty();
        }

        @Test
        @DisplayName("no detecta el patrón cuando hay menos lecturas que el mínimo requerido")
        void doesNotDetectWhenBelowMinCount() {
            when(systemConfig.getInt("pattern.fasting_threshold_mgdl")).thenReturn(130);
            when(systemConfig.getDecimal("pattern.fasting_ratio")).thenReturn(0.6);
            when(systemConfig.getInt("alert.min_readings_for_stats")).thenReturn(3);

            List<GlucoseReading> readings = readingsOfType(ReadingType.FASTING, 200, 200);

            Optional<Alert> alert = service.detectHighFastingPattern(readings);

            assertThat(alert).isEmpty();
        }

        @Test
        @DisplayName("ignora lecturas que no son de tipo FASTING")
        void ignoresNonFastingReadings() {
            when(systemConfig.getInt("pattern.fasting_threshold_mgdl")).thenReturn(130);
            when(systemConfig.getDecimal("pattern.fasting_ratio")).thenReturn(0.6);
            when(systemConfig.getInt("alert.min_readings_for_stats")).thenReturn(3);

            List<GlucoseReading> readings = readingsOfType(ReadingType.RANDOM, 200, 200, 200, 200, 200);

            Optional<Alert> alert = service.detectHighFastingPattern(readings);

            assertThat(alert).isEmpty();
        }
    }

    @Nested
    @DisplayName("detectHighPostMealPattern")
    class DetectHighPostMealPattern {

        @Test
        @DisplayName("detecta el patrón cuando el 50% o más de las postprandiales superan el umbral")
        void detectsPatternWhenRatioMet() {
            when(systemConfig.getInt("pattern.postmeal_threshold_mgdl")).thenReturn(180);
            when(systemConfig.getDecimal("pattern.postmeal_ratio")).thenReturn(0.5);
            when(systemConfig.getInt("alert.min_readings_for_stats")).thenReturn(3);

            // 2 de 4 por encima de 180 = 50%
            List<GlucoseReading> readings = readingsOfType(ReadingType.POST_MEAL, 200, 190, 170, 150);

            Optional<Alert> alert = service.detectHighPostMealPattern(readings);

            assertThat(alert).isPresent();
            assertThat(alert.get().getSeverity()).isEqualTo(Alert.Severity.WARNING);
        }

        @Test
        @DisplayName("no detecta el patrón cuando el ratio está por debajo del umbral")
        void doesNotDetectWhenRatioBelowThreshold() {
            when(systemConfig.getInt("pattern.postmeal_threshold_mgdl")).thenReturn(180);
            when(systemConfig.getDecimal("pattern.postmeal_ratio")).thenReturn(0.5);
            when(systemConfig.getInt("alert.min_readings_for_stats")).thenReturn(3);

            // 1 de 4 por encima de 180 = 25%
            List<GlucoseReading> readings = readingsOfType(ReadingType.POST_MEAL, 200, 170, 150, 140);

            Optional<Alert> alert = service.detectHighPostMealPattern(readings);

            assertThat(alert).isEmpty();
        }
    }

    @Nested
    @DisplayName("detectRecurrentHypoglycemia")
    class DetectRecurrentHypoglycemia {

        @Test
        @DisplayName("detecta el patrón cuando hay al menos el mínimo de episodios configurado")
        void detectsPatternWhenMinEpisodesMet() {
            when(systemConfig.getInt("pattern.hypo_min_episodes")).thenReturn(3);
            when(systemConfig.getInt("pattern.days_window")).thenReturn(14);

            List<GlucoseReading> readings = readingsOfType(ReadingType.RANDOM, 65, 60, 68, 150, 160);

            Optional<Alert> alert = service.detectRecurrentHypoglycemia(readings);

            assertThat(alert).isPresent();
            assertThat(alert.get().getSeverity()).isEqualTo(Alert.Severity.DANGER);
        }

        @Test
        @DisplayName("no detecta el patrón cuando hay menos episodios que el mínimo")
        void doesNotDetectWhenBelowMinEpisodes() {
            when(systemConfig.getInt("pattern.hypo_min_episodes")).thenReturn(3);

            List<GlucoseReading> readings = readingsOfType(ReadingType.RANDOM, 65, 60, 150, 160);

            Optional<Alert> alert = service.detectRecurrentHypoglycemia(readings);

            assertThat(alert).isEmpty();
        }

        @Test
        @DisplayName("considera hipoglucemia exclusivamente valores por debajo de 70 mg/dL")
        void considersOnlyValuesBelow70() {
            when(systemConfig.getInt("pattern.hypo_min_episodes")).thenReturn(3);

            // Exactamente 70 no cuenta como hipoglucemia (límite exclusivo)
            List<GlucoseReading> readings = readingsOfType(ReadingType.RANDOM, 70, 70, 70, 70);

            Optional<Alert> alert = service.detectRecurrentHypoglycemia(readings);

            assertThat(alert).isEmpty();
        }
    }

    @Nested
    @DisplayName("detectHighVariability")
    class DetectHighVariability {

        @Test
        @DisplayName("detecta alta variabilidad cuando el coeficiente de variación supera el umbral")
        void detectsHighVariability() {
            when(systemConfig.getInt("pattern.min_readings_variability")).thenReturn(7);
            when(systemConfig.getDecimal("pattern.cv_threshold")).thenReturn(36.0);

            // Valores muy dispersos -> CV ≈ 56%
            List<GlucoseReading> readings = readingsOfType(
                    ReadingType.RANDOM, 60, 250, 80, 220, 70, 240, 90);

            Optional<Alert> alert = service.detectHighVariability(readings);

            assertThat(alert).isPresent();
            assertThat(alert.get().getSeverity()).isEqualTo(Alert.Severity.WARNING);
        }

        @Test
        @DisplayName("no detecta alta variabilidad cuando los valores son consistentes")
        void doesNotDetectWhenValuesAreConsistent() {
            when(systemConfig.getInt("pattern.min_readings_variability")).thenReturn(7);
            when(systemConfig.getDecimal("pattern.cv_threshold")).thenReturn(36.0);

            // Valores muy parejos -> CV ≈ 2%
            List<GlucoseReading> readings = readingsOfType(
                    ReadingType.RANDOM, 100, 105, 98, 102, 100, 103, 99);

            Optional<Alert> alert = service.detectHighVariability(readings);

            assertThat(alert).isEmpty();
        }

        @Test
        @DisplayName("no detecta cuando hay menos lecturas que el mínimo requerido")
        void doesNotDetectWhenBelowMinReadings() {
            when(systemConfig.getInt("pattern.min_readings_variability")).thenReturn(7);
            when(systemConfig.getDecimal("pattern.cv_threshold")).thenReturn(36.0);

            List<GlucoseReading> readings = readingsOfType(ReadingType.RANDOM, 60, 250, 80);

            Optional<Alert> alert = service.detectHighVariability(readings);

            assertThat(alert).isEmpty();
        }
    }


    private List<GlucoseReading> readingsOfType(ReadingType type, int... values) {
        return java.util.Arrays.stream(values)
                .mapToObj(v -> GlucoseReading.builder()
                        .readingId(UUID.randomUUID())
                        .patientId(patientId)
                        .value(BigDecimal.valueOf(v))
                        .unit(GlucoseUnit.MG_DL)
                        .readingType(type)
                        .measuredAt(LocalDateTime.now())
                        .build())
                .toList();
    }
}
