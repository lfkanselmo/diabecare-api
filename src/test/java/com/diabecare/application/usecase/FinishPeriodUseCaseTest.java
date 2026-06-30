package com.diabecare.application.usecase;

import com.diabecare.application.port.in.FinishPeriodUseCase;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.SaveMenstrualCyclePort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.model.MenstrualCycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinishPeriodUseCaseImpl")
class FinishPeriodUseCaseTest {

    @Mock
    private LoadMenstrualCyclePort loadMenstrualCyclePort;
    @Mock
    private SaveMenstrualCyclePort saveMenstrualCyclePort;

    @InjectMocks
    private FinishPeriodUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando no hay período en curso")
        void throwsWhenNoOngoingPeriod() {
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());

            FinishPeriodUseCase.Command command = new FinishPeriodUseCase.Command(patientId, LocalDate.now());

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("período en curso para finalizar");

            verifyNoInteractions(saveMenstrualCyclePort);
        }

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando el último ciclo ya está cerrado")
        void throwsWhenLastCycleAlreadyFinished() {
            MenstrualCycle finished = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.now().minusDays(10), null);
            finished.finish(LocalDate.now().minusDays(5));
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(finished));

            FinishPeriodUseCase.Command command = new FinishPeriodUseCase.Command(patientId, LocalDate.now());

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(InvalidPatientDataException.class);
        }

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando la fecha de fin es anterior al inicio")
        void throwsWhenEndDateBeforeStartDate() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.now().minusDays(3), null);
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));

            FinishPeriodUseCase.Command command = new FinishPeriodUseCase.Command(
                    patientId, LocalDate.now().minusDays(10));

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("anterior a la fecha de inicio");
        }

        @Test
        @DisplayName("finaliza el período correctamente con la fecha de fin indicada")
        void finishesPeriodCorrectly() {
            LocalDate startDate = LocalDate.now().minusDays(5);
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, startDate, null);
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(saveMenstrualCyclePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            FinishPeriodUseCase.Command command = new FinishPeriodUseCase.Command(patientId, LocalDate.now());

            MenstrualCycle result = useCase.execute(command);

            assertThat(result.isOngoing()).isFalse();
            assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("acepta la fecha de fin igual a la fecha de inicio")
        void acceptsEndDateEqualToStartDate() {
            LocalDate startDate = LocalDate.now();
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, startDate, null);
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(saveMenstrualCyclePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            FinishPeriodUseCase.Command command = new FinishPeriodUseCase.Command(patientId, startDate);

            assertThatCode(() -> useCase.execute(command)).doesNotThrowAnyException();
        }
    }
}