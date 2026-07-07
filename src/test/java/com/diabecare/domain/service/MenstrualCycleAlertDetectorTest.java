package com.diabecare.domain.service;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
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
@DisplayName("MenstrualCycleAlertDetector")
class MenstrualCycleAlertDetectorTest {

    @Mock private LoadMenstrualCyclePort loadMenstrualCyclePort;
    @Mock private AlertConfigPort alertConfig;

    private MenstrualCycleAlertDetector detector;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        detector = new MenstrualCycleAlertDetector(
                loadMenstrualCyclePort,
                new CycleStatisticsService(),
                (key, args) -> "mensaje",
                new MenstrualCycleGuidanceService((key, args) -> "guía"),
                alertConfig);
    }

    @Test
    @DisplayName("no consulta el puerto de ciclo para pacientes no femeninas")
    void skipsNonFemalePatients() {
        Patient male = buildPatient(BiologicalSex.MALE);

        List<Alert> alerts = detector.detect(male, LocalDateTime.now());

        assertThat(alerts).isEmpty();
        verifyNoInteractions(loadMenstrualCyclePort);
    }

    @Test
    @DisplayName("retorna lista vacía cuando la paciente no tiene ciclos registrados")
    void returnsEmptyWhenNoCycleRegistered() {
        Patient female = buildPatient(BiologicalSex.FEMALE);
        when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());

        List<Alert> alerts = detector.detect(female, LocalDateTime.now());

        assertThat(alerts).isEmpty();
    }

    @Nested
    @DisplayName("con un ciclo en curso registrado")
    class WithOngoingCycle {

        private final Patient patient = buildPatient(BiologicalSex.FEMALE);

        @BeforeEach
        void stubConfig() {
            lenient().when(alertConfig.daysBeforeOpenCycleAlert()).thenReturn(10);
        }

        @Test
        @DisplayName("genera alerta de fase con severidad INFO en el día 14 (ovulación)")
        void generatesOvulationPhaseAlert() {
            List<Alert> alerts = detectFor(ongoingCycleStartedDaysAgo(13));

            assertThat(alerts).anyMatch(a ->
                    "guía".equals(a.getMessage()) && a.getSeverity() == Alert.Severity.INFO);
        }

        @Test
        @DisplayName("genera alerta de fase con severidad WARNING pasado el 75% del ciclo (lútea tardía)")
        void generatesLutealLatePhaseAlert() {
            List<Alert> alerts = detectFor(ongoingCycleStartedDaysAgo(24));

            assertThat(alerts).anyMatch(a ->
                    "guía".equals(a.getMessage()) && a.getSeverity() == Alert.Severity.WARNING);
        }

        @Test
        @DisplayName("genera OPEN_CYCLE_REMINDER cuando el período lleva abierto más de lo configurado")
        void generatesOpenTooLongAlert() {
            List<Alert> alerts = detectFor(ongoingCycleStartedDaysAgo(24));

            assertThat(alerts).anyMatch(a -> a.getType() == Alert.AlertType.OPEN_CYCLE_REMINDER);
        }

        @Test
        @DisplayName("no genera OPEN_CYCLE_REMINDER cuando el período empezó hace menos días de lo configurado")
        void doesNotGenerateOpenTooLongAlertWhenRecent() {
            List<Alert> alerts = detectFor(ongoingCycleStartedDaysAgo(2));

            assertThat(alerts).noneMatch(a -> a.getType() == Alert.AlertType.OPEN_CYCLE_REMINDER);
        }

        @Test
        @DisplayName("genera alerta de próximo período a 3 días con longitud de ciclo por defecto")
        void generatesUpcomingPeriodAlertAt3Days() {
            // Longitud de ciclo por defecto (28) + inicio hace 25 días → próximo período en 3 días.
            List<Alert> alerts = detectFor(ongoingCycleStartedDaysAgo(25));

            assertThat(alerts).anyMatch(a ->
                    "mensaje".equals(a.getMessage()) && a.getSeverity() == Alert.Severity.INFO);
        }

        private List<Alert> detectFor(MenstrualCycle cycle) {
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of(cycle));
            return detector.detect(patient, LocalDateTime.now());
        }
    }

    private MenstrualCycle ongoingCycleStartedDaysAgo(int days) {
        return MenstrualCycle.builder()
                .cycleId(UUID.randomUUID())
                .patientId(patientId)
                .startDate(LocalDate.now().minusDays(days))
                .build();
    }

    private Patient buildPatient(BiologicalSex sex) {
        return Patient.builder()
                .patientId(patientId)
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
                .biologicalSex(sex)
                .build();
    }
}
