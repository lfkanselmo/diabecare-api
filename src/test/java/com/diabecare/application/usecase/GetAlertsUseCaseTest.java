package com.diabecare.application.usecase;

import com.diabecare.application.port.out.*;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.CycleStatisticsService;
import com.diabecare.domain.service.MedicalCalculatorService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.diabecare.domain.service.PatternDetectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GetAlertsUseCase")
class GetAlertsUseCaseTest {

    @Mock private LoadPatientPort        loadPatientPort;
    @Mock private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock private LoadMealEntryPort      loadMealEntryPort;
    @Mock private LoadMenstrualCyclePort loadMenstrualCyclePort;
    @Mock private AlertConfigPort        alertConfig;
    @Mock private PatternDetectorService patternDetectorService;
    @Mock private SystemConfigPort       systemConfig;
    @Mock private MessageResolverPort    messages;
    @Mock private MenstrualCycleGuidanceService cycleGuidanceService;
    @Mock private CycleStatisticsService cycleStatisticsService;

    private GetAlertsUseCaseImpl useCase;
    private UUID patientId;
    private Patient patient;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        patient   = buildPatient(patientId, BiologicalSex.NOT_SPECIFIED);

        when(alertConfig.hoursWithoutGlucoseAlert()).thenReturn(8);
        when(alertConfig.minReadingsForStats()).thenReturn(3);
        when(alertConfig.goodTirThreshold()).thenReturn(70.0);
        when(alertConfig.streakDays()).thenReturn(3);
        when(alertConfig.daysBeforeOpenCycleAlert()).thenReturn(10);

        when(systemConfig.getInt(any())).thenReturn(7);
        when(systemConfig.getDecimal(any())).thenReturn(7.0);

        when(messages.resolve(any())).thenReturn("mensaje");
        when(messages.resolve(any(), any())).thenReturn("mensaje");

        when(patternDetectorService.detectHighFastingPattern(any())).thenReturn(Optional.empty());
        when(patternDetectorService.detectHighPostMealPattern(any())).thenReturn(Optional.empty());
        when(patternDetectorService.detectRecurrentHypoglycemia(any())).thenReturn(Optional.empty());
        when(patternDetectorService.detectHighVariability(any())).thenReturn(Optional.empty());

        when(loadMenstrualCyclePort.findByPatientId(any())).thenReturn(List.of());
        when(cycleStatisticsService.calculateAverageCycleLength(any())).thenReturn(null);
        when(cycleStatisticsService.calculateAveragePeriodLength(any())).thenReturn(null);

        useCase = new GetAlertsUseCaseImpl(
                loadPatientPort,
                loadGlucoseReadingPort,
                loadMealEntryPort,
                loadMenstrualCyclePort,
                new MedicalCalculatorService(),
                alertConfig,
                patternDetectorService,
                systemConfig,
                messages,
                cycleGuidanceService,
                cycleStatisticsService
        );
    }

    @Test
    @DisplayName("genera alerta NO_GLUCOSE_RECORDED cuando no hay lecturas recientes")
    void generatesNoGlucoseAlert() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of());
        when(loadMealEntryPort.findByPatientIdAndDate(any(), any()))
                .thenReturn(List.of());

        List<Alert> alerts = useCase.getAlerts(patientId);

        assertThat(alerts).anyMatch(a ->
                a.getType() == Alert.AlertType.NO_GLUCOSE_RECORDED);
    }

    @Test
    @DisplayName("genera alerta GLUCOSE_OUT_OF_RANGE cuando la última lectura es alta")
    void generatesHighGlucoseAlert() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

        GlucoseReading highReading = buildReading(patientId, 250.0);
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(highReading));
        when(loadMealEntryPort.findByPatientIdAndDate(any(), any()))
                .thenReturn(List.of());

        List<Alert> alerts = useCase.getAlerts(patientId);

        assertThat(alerts).anyMatch(a ->
                a.getType() == Alert.AlertType.GLUCOSE_OUT_OF_RANGE &&
                        a.getSeverity() == Alert.Severity.WARNING);
    }

    @Test
    @DisplayName("genera alerta POSITIVE_STREAK cuando el TIR es bueno")
    void generatesPositiveStreakAlert() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

        List<GlucoseReading> goodReadings = List.of(
                buildReading(patientId, 100), buildReading(patientId, 110),
                buildReading(patientId, 120), buildReading(patientId, 130),
                buildReading(patientId, 140), buildReading(patientId, 150),
                buildReading(patientId, 160)
        );

        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                .thenReturn(goodReadings);
        when(loadMealEntryPort.findByPatientIdAndDate(any(), any()))
                .thenReturn(List.of());

        List<Alert> alerts = useCase.getAlerts(patientId);

        assertThat(alerts).anyMatch(a ->
                a.getType() == Alert.AlertType.POSITIVE_STREAK &&
                        a.getSeverity() == Alert.Severity.SUCCESS);
    }

    @Test
    @DisplayName("no llama al puerto de ciclo menstrual para pacientes masculinos")
    void doesNotCallMenstrualPortForMale() {
        Patient malePatient = buildPatient(patientId, BiologicalSex.MALE);

        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(malePatient));
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(buildReading(patientId, 100)));
        when(loadMealEntryPort.findByPatientIdAndDate(any(), any()))
                .thenReturn(List.of());

        useCase.getAlerts(patientId);

        verify(loadMenstrualCyclePort, never()).findLatestByPatientId(any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Patient buildPatient(UUID id, BiologicalSex sex) {
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
                .dailyCalorieGoal(2000)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .preferredGlucoseUnit(GlucoseUnit.MG_DL)
                .biologicalSex(sex)
                .build();
    }

    private GlucoseReading buildReading(UUID patientId, double value) {
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