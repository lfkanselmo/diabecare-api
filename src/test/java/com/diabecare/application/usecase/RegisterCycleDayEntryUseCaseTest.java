package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterCycleDayEntryUseCase;
import com.diabecare.application.port.out.LoadCycleDayEntryPort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.SaveCycleDayEntryPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CycleSymptom;
import com.diabecare.domain.model.FlowIntensity;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.SymptomSeverity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterCycleDayEntryUseCaseImpl")
class RegisterCycleDayEntryUseCaseTest {

    @Mock
    private LoadMenstrualCyclePort loadMenstrualCyclePort;
    @Mock
    private LoadCycleDayEntryPort loadCycleDayEntryPort;
    @Mock
    private SaveCycleDayEntryPort saveCycleDayEntryPort;

    @InjectMocks
    private RegisterCycleDayEntryUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando no hay ciclo en curso")
        void throwsWhenNoOngoingCycle() {
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());

            RegisterCycleDayEntryUseCase.Command command = new RegisterCycleDayEntryUseCase.Command(
                    patientId, LocalDate.now(), FlowIntensity.MODERATE, null, List.of());

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("ciclo en curso");

            verifyNoInteractions(saveCycleDayEntryPort);
        }

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando el último ciclo ya está cerrado")
        void throwsWhenLastCycleIsFinished() {
            MenstrualCycle finished = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.now().minusDays(10), null);
            finished.finish(LocalDate.now().minusDays(5));
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(finished));

            RegisterCycleDayEntryUseCase.Command command = new RegisterCycleDayEntryUseCase.Command(
                    patientId, LocalDate.now(), FlowIntensity.MODERATE, null, List.of());

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(InvalidPatientDataException.class);
        }

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando la fecha es anterior al inicio del ciclo")
        void throwsWhenEntryDateBeforeCycleStart() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.now().minusDays(3), null);
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));

            RegisterCycleDayEntryUseCase.Command command = new RegisterCycleDayEntryUseCase.Command(
                    patientId, LocalDate.now().minusDays(10), FlowIntensity.MODERATE, null, List.of());

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("anterior al inicio");
        }

        @Test
        @DisplayName("registra un nuevo día del ciclo con sus síntomas correctamente")
        void registersNewDayEntryWithSymptoms() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.now().minusDays(3), null);
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadCycleDayEntryPort.findByCycleIdAndDate(cycle.getCycleId(), LocalDate.now()))
                    .thenReturn(Optional.empty());
            when(saveCycleDayEntryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterCycleDayEntryUseCase.Command command = new RegisterCycleDayEntryUseCase.Command(
                    patientId, LocalDate.now(), FlowIntensity.MODERATE, "día 4",
                    List.of(new RegisterCycleDayEntryUseCase.SymptomInput(
                            CycleSymptom.CRAMPS, SymptomSeverity.MODERATE)));

            CycleDayEntry result = useCase.execute(command);

            assertThat(result.getFlowIntensity()).isEqualTo(FlowIntensity.MODERATE);
            assertThat(result.getSymptoms()).hasSize(1);
            assertThat(result.getSymptoms().get(0).getSymptom()).isEqualTo(CycleSymptom.CRAMPS);
        }

        @Test
        @DisplayName("actualiza el registro existente del mismo día en vez de crear uno nuevo")
        void updatesExistingEntryForSameDay() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.now().minusDays(3), null);
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));

            CycleDayEntry existing = CycleDayEntry.create(
                    cycle.getCycleId(), patientId, LocalDate.now(), FlowIntensity.LIGHT, "nota vieja", null);
            when(loadCycleDayEntryPort.findByCycleIdAndDate(cycle.getCycleId(), LocalDate.now()))
                    .thenReturn(Optional.of(existing));
            when(saveCycleDayEntryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterCycleDayEntryUseCase.Command command = new RegisterCycleDayEntryUseCase.Command(
                    patientId, LocalDate.now(), FlowIntensity.HEAVY, "nota actualizada", List.of());

            CycleDayEntry result = useCase.execute(command);

            assertThat(result.getDayEntryId()).isEqualTo(existing.getDayEntryId());
            assertThat(result.getFlowIntensity()).isEqualTo(FlowIntensity.HEAVY);
            assertThat(result.getNotes()).isEqualTo("nota actualizada");
        }

        @Test
        @DisplayName("registra correctamente cuando no se especifican síntomas")
        void registersCorrectlyWithoutSymptoms() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.now().minusDays(3), null);
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadCycleDayEntryPort.findByCycleIdAndDate(any(), any())).thenReturn(Optional.empty());
            when(saveCycleDayEntryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterCycleDayEntryUseCase.Command command = new RegisterCycleDayEntryUseCase.Command(
                    patientId, LocalDate.now(), FlowIntensity.LIGHT, null, null);

            CycleDayEntry result = useCase.execute(command);

            assertThat(result.getSymptoms()).isEmpty();
        }
    }
}