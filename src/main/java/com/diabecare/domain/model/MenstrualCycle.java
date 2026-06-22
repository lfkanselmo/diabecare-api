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
}