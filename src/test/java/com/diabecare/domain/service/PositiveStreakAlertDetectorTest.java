package com.diabecare.domain.service;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositiveStreakAlertDetector")
class PositiveStreakAlertDetectorTest {

    @Mock private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock private AlertConfigPort alertConfig;

    private PositiveStreakAlertDetector detector;
    private final UUID patientId = UUID.randomUUID();
    private final Patient patient = buildPatient(patientId);

    @BeforeEach
    void setUp() {
        detector = new PositiveStreakAlertDetector(
                loadGlucoseReadingPort, alertConfig, new MedicalCalculatorService(), (key, args) -> "mensaje");
        lenient().when(alertConfig.streakDays()).thenReturn(7);
        lenient().when(alertConfig.minReadingsForStats()).thenReturn(3);
        lenient().when(alertConfig.goodTirThreshold()).thenReturn(70.0);
    }

    @Test
    @DisplayName("genera POSITIVE_STREAK cuando el TIR supera el umbral configurado")
    void generatesPositiveStreakWhenTirIsGood() {
        List<GlucoseReading> goodReadings = List.of(
                buildReading(100), buildReading(110), buildReading(120),
                buildReading(130), buildReading(140), buildReading(150));
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                .thenReturn(goodReadings);

        List<Alert> alerts = detector.detect(patient, LocalDateTime.now());

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(Alert.AlertType.POSITIVE_STREAK);
        assertThat(alerts.get(0).getSeverity()).isEqualTo(Alert.Severity.SUCCESS);
    }

    @Test
    @DisplayName("no genera alerta cuando no hay suficientes lecturas")
    void noAlertWhenNotEnoughReadings() {
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(buildReading(100)));

        assertThat(detector.detect(patient, LocalDateTime.now())).isEmpty();
    }

    @Test
    @DisplayName("no genera alerta cuando el TIR está por debajo del umbral")
    void noAlertWhenTirBelowThreshold() {
        List<GlucoseReading> badReadings = List.of(
                buildReading(300), buildReading(310), buildReading(320), buildReading(330));
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                .thenReturn(badReadings);

        assertThat(detector.detect(patient, LocalDateTime.now())).isEmpty();
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
