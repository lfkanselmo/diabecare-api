package com.diabecare.domain.service;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.SystemConfigPort;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClinicalPatternAlertDetector")
class ClinicalPatternAlertDetectorTest {

    @Mock private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock private AlertConfigPort alertConfig;
    @Mock private SystemConfigPort systemConfig;
    @Mock private PatternDetectorService patternDetectorService;

    private ClinicalPatternAlertDetector detector;
    private final UUID patientId = UUID.randomUUID();
    private final Patient patient = buildPatient(patientId);

    @BeforeEach
    void setUp() {
        detector = new ClinicalPatternAlertDetector(
                loadGlucoseReadingPort, alertConfig, systemConfig, patternDetectorService);
        lenient().when(systemConfig.getInt("pattern.days_window")).thenReturn(14);
        lenient().when(alertConfig.minReadingsForStats()).thenReturn(3);
    }

    @Test
    @DisplayName("retorna vacío cuando no hay suficientes lecturas en la ventana")
    void returnsEmptyWhenNotEnoughReadings() {
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(buildReading(100)));

        assertThat(detector.detect(patient, LocalDateTime.now())).isEmpty();
        verifyNoInteractions(patternDetectorService);
    }

    @Test
    @DisplayName("delega en PatternDetectorService y filtra los patrones no detectados")
    void delegatesToPatternDetectorServiceAndFiltersEmpty() {
        List<GlucoseReading> readings = List.of(buildReading(100), buildReading(110), buildReading(120));
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(readings);

        Alert fastingAlert = Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.WARNING)
                .title("t").message("m").build();

        when(patternDetectorService.detectHighFastingPattern(readings)).thenReturn(Optional.of(fastingAlert));
        when(patternDetectorService.detectHighPostMealPattern(readings)).thenReturn(Optional.empty());
        when(patternDetectorService.detectRecurrentHypoglycemia(readings)).thenReturn(Optional.empty());
        when(patternDetectorService.detectHighVariability(readings)).thenReturn(Optional.empty());

        List<Alert> alerts = detector.detect(patient, LocalDateTime.now());

        assertThat(alerts).containsExactly(fastingAlert);
    }

    @Test
    @DisplayName("combina múltiples patrones detectados a la vez")
    void combinesMultipleDetectedPatterns() {
        List<GlucoseReading> readings = List.of(buildReading(100), buildReading(110), buildReading(120));
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(readings);

        Alert a1 = Alert.builder().type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.WARNING).title("t1").message("m1").build();
        Alert a2 = Alert.builder().type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.DANGER).title("t2").message("m2").build();

        when(patternDetectorService.detectHighFastingPattern(readings)).thenReturn(Optional.of(a1));
        when(patternDetectorService.detectHighPostMealPattern(readings)).thenReturn(Optional.empty());
        when(patternDetectorService.detectRecurrentHypoglycemia(readings)).thenReturn(Optional.of(a2));
        when(patternDetectorService.detectHighVariability(readings)).thenReturn(Optional.empty());

        assertThat(detector.detect(patient, LocalDateTime.now())).containsExactly(a1, a2);
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
