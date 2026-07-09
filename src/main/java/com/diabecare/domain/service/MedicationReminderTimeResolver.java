package com.diabecare.domain.service;

import com.diabecare.domain.model.MedicationFrequency;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Traduce la frecuencia de un medicamento a los horarios del día en que debería
 * recordarse tomarlo. No hay registro de "dosis tomada" en este dominio, así que
 * el recordatorio es puramente horario — no se suprime si el paciente ya tomó
 * la dosis.
 */
public class MedicationReminderTimeResolver {

    private static final Map<MedicationFrequency, List<LocalTime>> TIMES_BY_FREQUENCY = Map.of(
            MedicationFrequency.ONCE_DAILY, List.of(LocalTime.of(8, 0)),
            MedicationFrequency.TWICE_DAILY, List.of(LocalTime.of(8, 0), LocalTime.of(20, 0)),
            MedicationFrequency.THREE_TIMES_DAILY,
                    List.of(LocalTime.of(8, 0), LocalTime.of(14, 0), LocalTime.of(20, 0)),
            MedicationFrequency.WITH_MEALS,
                    List.of(LocalTime.of(7, 0), LocalTime.of(12, 0), LocalTime.of(19, 0)),
            MedicationFrequency.BEFORE_MEALS,
                    List.of(LocalTime.of(6, 30), LocalTime.of(11, 30), LocalTime.of(18, 30)),
            MedicationFrequency.AT_BEDTIME, List.of(LocalTime.of(22, 0))
            // AS_NEEDED no tiene horario fijo — deliberadamente ausente del mapa.
    );

    public List<LocalTime> resolveTimes(MedicationFrequency frequency) {
        return TIMES_BY_FREQUENCY.getOrDefault(frequency, List.of());
    }
}
