package com.diabecare.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MenstrualCycle")
class MenstrualCycleTest {

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("startNewCycle")
    class StartNewCycle {

        @Test
        @DisplayName("crea un ciclo nuevo en curso, sin fecha de fin")
        void createsOngoingCycle() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.of(2026, 6, 1), "notas");

            assertThat(cycle.getCycleId()).isNotNull();
            assertThat(cycle.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(cycle.isOngoing()).isTrue();
            assertThat(cycle.getEndDate()).isNull();
        }
    }

    @Nested
    @DisplayName("finish / getActualPeriodLengthDays")
    class Finish {

        @Test
        @DisplayName("al finalizar, el ciclo deja de estar en curso")
        void finishingMakesCycleNotOngoing() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            assertThat(cycle.isOngoing()).isFalse();
            assertThat(cycle.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 6));
        }

        @Test
        @DisplayName("calcula la duración real del período de forma inclusiva")
        void calculatesActualPeriodLengthInclusively() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            // 1 al 6 de junio inclusive = 6 días
            assertThat(cycle.getActualPeriodLengthDays()).isEqualTo(6);
        }

        @Test
        @DisplayName("retorna null cuando el ciclo sigue en curso")
        void returnsNullWhenStillOngoing() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            assertThat(cycle.getActualPeriodLengthDays()).isNull();
        }
    }

    @Nested
    @DisplayName("calculateCurrentPhase — ciclo en curso")
    class CalculateCurrentPhaseOngoing {

        @Test
        @DisplayName("clasifica como MENSTRUATION durante los primeros días")
        void classifiesAsMenstruation() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            CyclePhase phase = cycle.calculateCurrentPhase(LocalDate.of(2026, 6, 3), null, null);
            assertThat(phase).isEqualTo(CyclePhase.MENSTRUATION);
        }

        @Test
        @DisplayName("clasifica como FOLLICULAR después del período, antes de ovulación")
        void classifiesAsFollicular() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            CyclePhase phase = cycle.calculateCurrentPhase(LocalDate.of(2026, 6, 10), null, null);
            assertThat(phase).isEqualTo(CyclePhase.FOLLICULAR);
        }

        @Test
        @DisplayName("clasifica como OVULATION exactamente en el día 14")
        void classifiesAsOvulation() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            CyclePhase phase = cycle.calculateCurrentPhase(LocalDate.of(2026, 6, 14), null, null);
            assertThat(phase).isEqualTo(CyclePhase.OVULATION);
        }

        @Test
        @DisplayName("clasifica como LUTEAL_EARLY después de la ovulación")
        void classifiesAsLutealEarly() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            CyclePhase phase = cycle.calculateCurrentPhase(LocalDate.of(2026, 6, 18), null, null);
            assertThat(phase).isEqualTo(CyclePhase.LUTEAL_EARLY);
        }

        @Test
        @DisplayName("clasifica como LUTEAL_LATE cerca del fin del ciclo")
        void classifiesAsLutealLate() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            CyclePhase phase = cycle.calculateCurrentPhase(LocalDate.of(2026, 6, 25), null, null);
            assertThat(phase).isEqualTo(CyclePhase.LUTEAL_LATE);
        }

        @Test
        @DisplayName("usa la duración real del período ya finalizado en vez del promedio")
        void usesActualPeriodLengthOverAverage() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 8)); // período real de 8 días

            // Día 7 debería seguir siendo MENSTRUATION porque el período real duró 8 días,
            // aunque el promedio pasado fuera distinto
            CyclePhase phase = cycle.calculateCurrentPhase(LocalDate.of(2026, 6, 7), 28, 5);
            assertThat(phase).isEqualTo(CyclePhase.MENSTRUATION);
        }
    }

    @Nested
    @DisplayName("calculateCurrentPhase / predictNextCycleStart — ciclo cerrado, caso real del bug corregido")
    class ClosedCycleRecentCase {

        @Test
        @DisplayName("BUG REGRESIVO: con un solo período cerrado reciente, predice 28 días tras el inicio real, no 56")
        void predictsOneCycleAheadNotTwo() {
            // Caso real reportado: ciclo del 01/06 al 06/06, sin historial adicional (avgCycleLength=null),
            // consultado el 27/06 (antes de que se cumplan los 28 días desde el inicio)
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            LocalDate today = LocalDate.of(2026, 6, 27);
            LocalDate nextPredicted = cycle.predictNextCycleStart(today, null);

            assertThat(nextPredicted).isEqualTo(LocalDate.of(2026, 6, 29));
        }

        @Test
        @DisplayName("el día de ciclo se cuenta desde el inicio real cuando aún no se alcanza la predicción")
        void dayOfCycleCountsFromRealStartBeforePrediction() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            int dayOfCycle = cycle.calculateDayOfCycle(LocalDate.of(2026, 6, 27), null);

            // del 1 al 27 de junio inclusive = día 27
            assertThat(dayOfCycle).isEqualTo(27);
        }

        @Test
        @DisplayName("la proyección no se considera desactualizada antes de alcanzar la fecha predicha")
        void projectionNotStaleBeforePrediction() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            boolean stale = cycle.isProjectionStale(LocalDate.of(2026, 6, 27), null);

            assertThat(stale).isFalse();
        }
    }

    @Nested
    @DisplayName("predictNextCycleStart / isProjectionStale — inactividad prolongada")
    class ProlongedInactivity {

        @Test
        @DisplayName("justo en la fecha predicha, el día de ciclo reinicia a 1")
        void dayOfCycleResetsExactlyAtPrediction() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            int dayOfCycle = cycle.calculateDayOfCycle(LocalDate.of(2026, 6, 29), null);
            assertThat(dayOfCycle).isEqualTo(1);
        }

        @Test
        @DisplayName("tras 4 meses de inactividad, el día de ciclo se mantiene acotado, sin crecer sin límite")
        void dayOfCycleStaysBoundedAfterMonthsOfInactivity() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            int dayOfCycle = cycle.calculateDayOfCycle(LocalDate.of(2026, 10, 15), null);

            assertThat(dayOfCycle).isBetween(1, 28);
        }

        @Test
        @DisplayName("tras 4 meses de inactividad, la próxima predicción sigue siendo una fecha futura")
        void nextPredictionRemainsInFutureAfterInactivity() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            LocalDate today = LocalDate.of(2026, 10, 15);
            LocalDate nextPredicted = cycle.predictNextCycleStart(today, null);

            assertThat(nextPredicted).isAfter(today);
        }

        @Test
        @DisplayName("tras saltar al menos un ciclo completo, la proyección se marca como desactualizada")
        void projectionMarkedStaleAfterSkippingACycle() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            // 28 días después de la primera predicción (29 jun) ya entra al segundo ciclo proyectado
            boolean stale = cycle.isProjectionStale(LocalDate.of(2026, 7, 27), null);

            assertThat(stale).isTrue();
        }

        @Test
        @DisplayName("un año después, el día de ciclo sigue siendo válido y acotado")
        void remainsValidAfterOneYear() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            LocalDate oneYearLater = LocalDate.of(2027, 6, 1);
            int dayOfCycle = cycle.calculateDayOfCycle(oneYearLater, null);
            LocalDate nextPredicted = cycle.predictNextCycleStart(oneYearLater, null);

            assertThat(dayOfCycle).isBetween(1, 28);
            assertThat(nextPredicted).isAfter(oneYearLater);
        }
    }

    @Nested
    @DisplayName("isProjectionStale — ciclo en curso")
    class IsProjectionStaleOngoing {

        @Test
        @DisplayName("siempre es false mientras el ciclo está en curso")
        void alwaysFalseWhileOngoing() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            boolean stale = cycle.isProjectionStale(LocalDate.of(2027, 6, 1), null);
            assertThat(stale).isFalse();
        }
    }

    @Nested
    @DisplayName("uso de averageCycleLength personalizado")
    class CustomAverageCycleLength {

        @Test
        @DisplayName("respeta un promedio de ciclo distinto al default de 28 días")
        void respectsCustomAverageCycleLength() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            LocalDate today = LocalDate.of(2026, 6, 15);
            LocalDate nextPredicted = cycle.predictNextCycleStart(today, 32);

            assertThat(nextPredicted).isEqualTo(LocalDate.of(2026, 7, 3));
        }
    }
}