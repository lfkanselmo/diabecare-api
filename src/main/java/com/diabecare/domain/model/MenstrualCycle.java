package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
@Builder
public class MenstrualCycle {

    private static final int DEFAULT_CYCLE_LENGTH = 28;
    private static final int DEFAULT_PERIOD_LENGTH = 5;

    private final UUID cycleId;
    private final UUID patientId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    public static MenstrualCycle startNewCycle(UUID patientId, LocalDate startDate, String notes) {
        return MenstrualCycle.builder()
                .cycleId(UUID.randomUUID())
                .patientId(patientId)
                .startDate(startDate)
                .notes(notes)
                .build();
    }

    public boolean isOngoing() {
        return endDate == null;
    }

    public void finish(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getActualPeriodLengthDays() {
        if (endDate == null) return null;
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public CyclePhase calculateCurrentPhase(LocalDate today, Integer averageCycleLength, Integer averagePeriodLength) {
        long dayOfCycle = ChronoUnit.DAYS.between(startDate, today) + 1;
        int length = averageCycleLength != null ? averageCycleLength : DEFAULT_CYCLE_LENGTH;
        int period = resolvePeriodLength(averagePeriodLength);

        if (dayOfCycle <= period)             return CyclePhase.MENSTRUATION;
        if (dayOfCycle <= 13)                  return CyclePhase.FOLLICULAR;
        if (dayOfCycle == 14)                  return CyclePhase.OVULATION;
        if (dayOfCycle <= (length * 0.75))     return CyclePhase.LUTEAL_EARLY;
        return CyclePhase.LUTEAL_LATE;
    }

    public LocalDate predictNextCycleStart(Integer averageCycleLength) {
        int length = averageCycleLength != null ? averageCycleLength : DEFAULT_CYCLE_LENGTH;
        return startDate.plusDays(length);
    }

    private int resolvePeriodLength(Integer averagePeriodLength) {
        Integer actual = getActualPeriodLengthDays();
        if (actual != null) return actual;
        if (averagePeriodLength != null) return averagePeriodLength;
        return DEFAULT_PERIOD_LENGTH;
    }
}