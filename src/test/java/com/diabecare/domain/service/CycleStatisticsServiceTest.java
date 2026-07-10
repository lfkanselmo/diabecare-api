package com.diabecare.domain.service;

import com.diabecare.domain.model.MenstrualCycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CycleStatisticsService")
class CycleStatisticsServiceTest {

    private final CycleStatisticsService service = new CycleStatisticsService();
    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("calculateAverageCycleLength")
    class CalculateAverageCycleLength {

        @Test
        @DisplayName("retorna null con historial vacío")
        void returnsNullWhenEmpty() {
            assertThat(service.calculateAverageCycleLength(List.of())).isNull();
        }

        @Test
        @DisplayName("retorna null con un solo ciclo (no hay diferencia que calcular)")
        void returnsNullWithSingleCycle() {
            List<MenstrualCycle> history = List.of(cycleStarting(LocalDate.of(2026, 6, 1)));
            assertThat(service.calculateAverageCycleLength(history)).isNull();
        }

        @Test
        @DisplayName("calcula el promedio correctamente con varios ciclos regulares")
        void calculatesAverageWithRegularCycles() {
            // Historial en orden descendente (más reciente primero), como lo entrega el repositorio
            List<MenstrualCycle> history = List.of(
                    cycleStarting(LocalDate.of(2026, 8, 26)), // 28 días desde el anterior
                    cycleStarting(LocalDate.of(2026, 7, 29)), // 28 días desde el anterior
                    cycleStarting(LocalDate.of(2026, 7, 1))
            );

            assertThat(service.calculateAverageCycleLength(history)).isEqualTo(28);
        }

        @Test
        @DisplayName("ignora diferencias fuera del rango fisiológico válido (21-45 días)")
        void ignoresOutOfPhysiologicalRangeDifferences() {
            List<MenstrualCycle> history = List.of(
                    cycleStarting(LocalDate.of(2026, 9, 1)),  // diferencia de 5 días con el siguiente (atípica, se ignora)
                    cycleStarting(LocalDate.of(2026, 8, 27)),
                    cycleStarting(LocalDate.of(2026, 8, 1)),  // diferencia de 26 días con el siguiente (válida)
                    cycleStarting(LocalDate.of(2026, 7, 2))   // diferencia de 30 días con el anterior (válida)
            );

            // Si se incluyera la diferencia atípica de 5 días, el promedio sería ~20.
            // Al filtrarla, el promedio real es (26+30)/2 = 28.
            assertThat(service.calculateAverageCycleLength(history)).isEqualTo(28);
        }

        @Test
        @DisplayName("retorna null cuando todas las diferencias están fuera del rango válido")
        void returnsNullWhenAllDifferencesInvalid() {
            List<MenstrualCycle> history = List.of(
                    cycleStarting(LocalDate.of(2026, 7, 10)), // 9 días, inválido
                    cycleStarting(LocalDate.of(2026, 7, 1))
            );

            assertThat(service.calculateAverageCycleLength(history)).isNull();
        }
    }

    @Nested
    @DisplayName("calculateAveragePeriodLength")
    class CalculateAveragePeriodLength {

        @Test
        @DisplayName("retorna null cuando no hay ciclos finalizados")
        void returnsNullWhenNoFinishedCycles() {
            MenstrualCycle ongoing = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 6, 1), null);
            assertThat(service.calculateAveragePeriodLength(List.of(ongoing))).isNull();
        }

        @Test
        @DisplayName("calcula el promedio correctamente usando solo los ciclos finalizados")
        void calculatesAverageUsingOnlyFinishedCycles() {
            MenstrualCycle finished1 = cycleFinished(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5)); // 5 días
            MenstrualCycle finished2 = cycleFinished(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7)); // 7 días
            MenstrualCycle ongoing = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 8, 1), null);

            Integer result = service.calculateAveragePeriodLength(List.of(finished1, finished2, ongoing));

            assertThat(result).isEqualTo(6);
        }

        @Test
        @DisplayName("ignora duraciones fuera del rango plausible (más de 14 días)")
        void ignoresImplausibleDurations() {
            MenstrualCycle normal = cycleFinished(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5)); // 5 días
            MenstrualCycle implausible = cycleFinished(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 20)); // 20 días

            Integer result = service.calculateAveragePeriodLength(List.of(normal, implausible));

            assertThat(result).isEqualTo(5);
        }
    }


    private MenstrualCycle cycleStarting(LocalDate startDate) {
        return MenstrualCycle.startNewCycle(patientId, startDate, null);
    }

    private MenstrualCycle cycleFinished(LocalDate startDate, LocalDate endDate) {
        MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, startDate, null);
        cycle.finish(endDate);
        return cycle;
    }
}
