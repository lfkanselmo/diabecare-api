package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
@Builder
public class MenstrualCycle {

    private final UUID cycleId;
    private final UUID patientId;
    private LocalDate cycleStartDate;
    private LocalDate cycleEndDate;
    private Integer cycleLengthDays;
    private Integer periodLengthDays;
    private CyclePhase phase;
    private String symptoms;
    private String notes;

    public static MenstrualCycle startNewCycle(UUID patientId,
                                               LocalDate startDate,
                                               Integer periodLengthDays,
                                               String symptoms,
                                               String notes) {
        return MenstrualCycle.builder()
                .cycleId(UUID.randomUUID())
                .patientId(patientId)
                .cycleStartDate(startDate)
                .periodLengthDays(periodLengthDays != null ? periodLengthDays : 5)
                .phase(CyclePhase.MENSTRUATION)
                .symptoms(symptoms)
                .notes(notes)
                .build();
    }

    public CyclePhase calculateCurrentPhase(LocalDate today) {
        long dayOfCycle = ChronoUnit.DAYS.between(cycleStartDate, today) + 1;
        int length = cycleLengthDays != null ? cycleLengthDays : 28;
        int period = periodLengthDays != null ? periodLengthDays : 5;

        if (dayOfCycle <= period)             return CyclePhase.MENSTRUATION;
        if (dayOfCycle <= 13)                  return CyclePhase.FOLLICULAR;
        if (dayOfCycle == 14)                  return CyclePhase.OVULATION;
        if (dayOfCycle <= (length * 0.75))     return CyclePhase.LUTEAL_EARLY;
        return CyclePhase.LUTEAL_LATE;
    }

    public LocalDate predictNextCycleStart() {
        int length = cycleLengthDays != null ? cycleLengthDays : 28;
        return cycleStartDate.plusDays(length);
    }

    public String getPhaseGlucoseGuidance() {
        return switch (calculateCurrentPhase(LocalDate.now())) {
            case MENSTRUATION ->
                    "Durante la menstruación la glucosa puede ser impredecible. " +
                            "Monitorea con más frecuencia.";
            case FOLLICULAR ->
                    "Fase folicular: mayor sensibilidad a la insulina. " +
                            "Es posible que necesites menos insulina.";
            case OVULATION ->
                    "Período de ovulación: el pico de estrógeno puede causar " +
                            "una bajada temporal de glucosa.";
            case LUTEAL_EARLY ->
                    "Fase lútea temprana: la progesterona empieza a aumentar la " +
                            "resistencia a la insulina. Monitorea de cerca.";
            case LUTEAL_LATE ->
                    "Fase lútea tardía: resistencia a la insulina en su punto " +
                            "máximo. Es normal necesitar más insulina estos días.";
        };
    }
}