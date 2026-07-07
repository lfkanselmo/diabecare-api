package com.diabecare.domain.service;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.SystemConfigPort;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlucoseRangeAlertDetector")
class GlucoseRangeAlertDetectorTest {

    @Mock private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock private AlertConfigPort alertConfig;
    @Mock private SystemConfigPort systemConfig;

    private GlucoseRangeAlertDetector detector;
    private final UUID patientId = UUID.randomUUID();
    private final Patient patient = buildPatient(patientId);
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        detector = new GlucoseRangeAlertDetector(
                loadGlucoseReadingPort, alertConfig, new MedicalCalculatorService(), systemConfig,
                (key, args) -> "mensaje");
        lenient().when(alertConfig.hoursWithoutGlucoseAlert()).thenReturn(8);
        lenient().when(alertConfig.minReadingsForStats()).thenReturn(3);
        lenient().when(systemConfig.getDecimal("alert.hba1c_threshold")).thenReturn(7.0);
    }

    @Nested
    @DisplayName("cuando no hay lecturas recientes")
    class NoRecentReadings {

        @Test
        @DisplayName("genera NO_GLUCOSE_RECORDED y no evalúa nada más")
        void generatesNoGlucoseAlert() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(patientId), any(), any()))
                    .thenReturn(List.of());

            List<Alert> alerts = detector.detect(patient, now);

            assertThat(alerts).hasSize(1);
            assertThat(alerts.get(0).getType()).isEqualTo(Alert.AlertType.NO_GLUCOSE_RECORDED);
        }
    }

    @Nested
    @DisplayName("cuando hay lecturas recientes")
    class WithRecentReadings {

        @Test
        @DisplayName("genera alerta DANGER cuando la última lectura es hipoglucemia")
        void generatesHypoAlert() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(patientId), any(), any()))
                    .thenReturn(List.of(buildReading(60)));

            List<Alert> alerts = detector.detect(patient, now);

            assertThat(alerts).anyMatch(a ->
                    a.getType() == Alert.AlertType.GLUCOSE_OUT_OF_RANGE
                            && a.getSeverity() == Alert.Severity.DANGER);
        }

        @Test
        @DisplayName("genera alerta WARNING cuando la última lectura supera el objetivo máximo")
        void generatesHighGlucoseAlert() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(patientId), any(), any()))
                    .thenReturn(List.of(buildReading(250)));

            List<Alert> alerts = detector.detect(patient, now);

            assertThat(alerts).anyMatch(a ->
                    a.getType() == Alert.AlertType.GLUCOSE_OUT_OF_RANGE
                            && a.getSeverity() == Alert.Severity.WARNING);
        }

        @Test
        @DisplayName("no genera alerta de rango cuando la lectura está dentro del objetivo")
        void noAlertWhenInRange() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(patientId), any(), any()))
                    .thenReturn(List.of(buildReading(100)));

            List<Alert> alerts = detector.detect(patient, now);

            assertThat(alerts).noneMatch(a -> a.getType() == Alert.AlertType.GLUCOSE_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("genera HIGH_HBA1C_ESTIMATED cuando el promedio semanal implica HbA1c elevada")
        void generatesHighHba1cAlert() {
            List<GlucoseReading> highWeekReadings = List.of(
                    buildReading(250), buildReading(240), buildReading(260));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(patientId), any(), any()))
                    .thenReturn(highWeekReadings);

            List<Alert> alerts = detector.detect(patient, now);

            assertThat(alerts).anyMatch(a -> a.getType() == Alert.AlertType.HIGH_HBA1C_ESTIMATED);
        }
    }

    private Patient buildPatient(UUID id) {
        return Patient.builder()
                .patientId(id)
                .userId(UUID.randomUUID())
                .fullName("Test Patient")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .diabetesType(DiabetesType.TYPE_2)
                .diagnosisDate(LocalDate.of(2020, 1, 1))
                .heightCm(new BigDecimal("170"))
                .targetGlucoseMin(new BigDecimal("70"))
                .targetGlucoseMax(new BigDecimal("180"))
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .preferredGlucoseUnit(GlucoseUnit.MG_DL)
                .biologicalSex(BiologicalSex.NOT_SPECIFIED)
                .build();
    }

    private GlucoseReading buildReading(double value) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(patientId)
                .value(BigDecimal.valueOf(value))
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(LocalDateTime.now().minusMinutes(30))
                .build();
    }
}
