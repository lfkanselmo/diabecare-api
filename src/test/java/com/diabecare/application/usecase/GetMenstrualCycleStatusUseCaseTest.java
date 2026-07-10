package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetMenstrualCycleStatusUseCase;
import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadCycleDayEntryPort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.CycleStatisticsService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMenstrualCycleStatusUseCaseImpl")
class GetMenstrualCycleStatusUseCaseTest {

    @Mock
    private LoadMenstrualCyclePort loadMenstrualCyclePort;
    @Mock
    private LoadCycleDayEntryPort loadCycleDayEntryPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private AlertConfigPort alertConfig;

    private GetMenstrualCycleStatusUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetMenstrualCycleStatusUseCaseImpl(
                loadMenstrualCyclePort, loadCycleDayEntryPort, loadPatientPort,
                new MenstrualCycleGuidanceService((key, args) -> "guía de prueba"),
                new CycleStatisticsService(), alertConfig);
    }

    @Nested
    @DisplayName("getStatus")
    class GetStatus {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getStatus(patientId))
                    .isInstanceOf(PatientNotFoundException.class);
        }

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando el paciente no es de sexo femenino")
        void throwsWhenPatientNotFemale() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));

            assertThatThrownBy(() -> useCase.getStatus(patientId))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("sexo femenino");
        }

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando no hay ciclos registrados")
        void throwsWhenNoCyclesRegistered() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(femalePatient()));
            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of());
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getStatus(patientId))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("Registra tu primer ciclo");
        }

        @Test
        @DisplayName("BUG REGRESIVO: con un ciclo cerrado reciente, predice 28 días tras el inicio, sin saltar un ciclo de más")
        void predictsCorrectDateWithRecentClosedCycle() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(femalePatient()));

            LocalDate startDate = LocalDate.now().minusDays(5);
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, startDate, null);
            cycle.finish(startDate.plusDays(5));

            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of(cycle));
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadCycleDayEntryPort.findByCycleIdAndDate(any(), any())).thenReturn(Optional.empty());

            GetMenstrualCycleStatusUseCase.CycleStatus status = useCase.getStatus(patientId);

            // El inicio fue hace 5 días; today aún no alcanza la predicción de 28 días desde el inicio.
            // El bug original saltaba a 56 días (2 ciclos); la corrección debe dar exactamente 28 días.
            assertThat(status.nextCycleStart()).isEqualTo(startDate.plusDays(28));
            assertThat(status.isProjectionStale()).isFalse();
        }

        @Test
        @DisplayName("no marca el ciclo como abierto demasiado tiempo cuando ya está finalizado")
        void doesNotMarkOpenTooLongWhenCycleIsFinished() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(femalePatient()));

            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.now().minusDays(20), null);
            cycle.finish(LocalDate.now().minusDays(15));

            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of(cycle));
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadCycleDayEntryPort.findByCycleIdAndDate(any(), any())).thenReturn(Optional.empty());

            GetMenstrualCycleStatusUseCase.CycleStatus status = useCase.getStatus(patientId);

            assertThat(status.isOpenTooLong()).isFalse();
            verifyNoInteractions(alertConfig);
        }

        @Test
        @DisplayName("marca el ciclo como abierto demasiado tiempo cuando supera el umbral configurado")
        void marksOpenTooLongWhenThresholdExceeded() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(femalePatient()));

            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.now().minusDays(15), null);

            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of(cycle));
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadCycleDayEntryPort.findByCycleIdAndDate(any(), any())).thenReturn(Optional.empty());
            when(alertConfig.daysBeforeOpenCycleAlert()).thenReturn(10);

            GetMenstrualCycleStatusUseCase.CycleStatus status = useCase.getStatus(patientId);

            assertThat(status.isOpenTooLong()).isTrue();
        }

        @Test
        @DisplayName("incluye el historial completo de ciclos en el resultado")
        void includesFullHistoryInResult() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(femalePatient()));

            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.now().minusDays(5), null);

            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of(cycle));
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadCycleDayEntryPort.findByCycleIdAndDate(any(), any())).thenReturn(Optional.empty());
            when(alertConfig.daysBeforeOpenCycleAlert()).thenReturn(10);

            GetMenstrualCycleStatusUseCase.CycleStatus status = useCase.getStatus(patientId);

            assertThat(status.history()).containsExactly(cycle);
        }
    }


    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }

    private Patient femalePatient() {
        Patient patient = validPatient();
        patient.updateBiologicalSex(BiologicalSex.FEMALE);
        return patient;
    }
}
