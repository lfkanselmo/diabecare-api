package com.diabecare.domain.service;

import com.diabecare.domain.model.MedicationFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MedicationReminderTimeResolver")
class MedicationReminderTimeResolverTest {

    private final MedicationReminderTimeResolver resolver = new MedicationReminderTimeResolver();

    @Test
    @DisplayName("retorna un solo horario para ONCE_DAILY")
    void returnsOneTimeForOnceDaily() {
        assertThat(resolver.resolveTimes(MedicationFrequency.ONCE_DAILY))
                .containsExactly(LocalTime.of(8, 0));
    }

    @Test
    @DisplayName("retorna dos horarios para TWICE_DAILY")
    void returnsTwoTimesForTwiceDaily() {
        assertThat(resolver.resolveTimes(MedicationFrequency.TWICE_DAILY)).hasSize(2);
    }

    @Test
    @DisplayName("retorna tres horarios para THREE_TIMES_DAILY")
    void returnsThreeTimesForThreeTimesDaily() {
        assertThat(resolver.resolveTimes(MedicationFrequency.THREE_TIMES_DAILY)).hasSize(3);
    }

    @Test
    @DisplayName("retorna lista vacía para AS_NEEDED, ya que no tiene horario fijo")
    void returnsEmptyListForAsNeeded() {
        assertThat(resolver.resolveTimes(MedicationFrequency.AS_NEEDED)).isEmpty();
    }

    @Test
    @DisplayName("cada frecuencia con horario retorna valores sin duplicados")
    void everyFrequencyWithScheduleReturnsDistinctValues() {
        for (MedicationFrequency frequency : MedicationFrequency.values()) {
            List<LocalTime> times = resolver.resolveTimes(frequency);
            assertThat(times).doesNotHaveDuplicates();
        }
    }
}
