package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetCyclePhaseCalendarUseCase;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.service.CycleStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCyclePhaseCalendarUseCaseImpl")
class GetCyclePhaseCalendarUseCaseTest {

    @Mock
    private LoadMenstrualCyclePort loadMenstrualCyclePort;

    private GetCyclePhaseCalendarUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetCyclePhaseCalendarUseCaseImpl(loadMenstrualCyclePort, new CycleStatisticsService());
    }

    @Nested
    @DisplayName("getCalendar")
    class GetCalendar {

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando no hay ciclos registrados")
        void throwsWhenNoCyclesRegistered() {
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getCalendar(
                    patientId, LocalDate.now(), LocalDate.now().plusDays(7)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("Registra tu primer ciclo");
        }

        @Test
        @DisplayName("genera un DayPhase por cada día del rango solicitado, inclusive")
        void generatesOneDayPhasePerDayInRangeInclusive() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.of(2026, 6, 1), null);

            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of(cycle));

            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 5);

            List<GetCyclePhaseCalendarUseCase.DayPhase> calendar = useCase.getCalendar(patientId, from, to);

            assertThat(calendar).hasSize(5);
            assertThat(calendar.get(0).date()).isEqualTo(from);
            assertThat(calendar.get(4).date()).isEqualTo(to);
        }

        @Test
        @DisplayName("asigna la fase correcta a cada día según el cálculo del ciclo")
        void assignsCorrectPhaseToEachDay() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.of(2026, 6, 1), null);

            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.of(cycle));
            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of(cycle));

            List<GetCyclePhaseCalendarUseCase.DayPhase> calendar = useCase.getCalendar(
                    patientId, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 1));

            assertThat(calendar.get(0).phase()).isEqualTo(
                    cycle.calculateCurrentPhase(LocalDate.of(2026, 6, 1), null, null));
        }
    }
}