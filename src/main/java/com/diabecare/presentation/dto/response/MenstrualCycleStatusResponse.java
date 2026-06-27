package com.diabecare.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MenstrualCycleStatusResponse(
        String currentPhase,
        String currentPhaseLabel,
        int dayOfCycle,
        boolean isOngoing,
        LocalDate periodStartDate,
        LocalDate nextCycleStart,
        int daysUntilNextCycle,
        String glucoseGuidance,
        Integer averageCycleLength,
        Integer averagePeriodLength,
        CycleDayEntryResponse todayEntry,
        List<CycleHistoryItem> history
) {
    public record CycleHistoryItem(
            String cycleId,
            LocalDate startDate,
            LocalDate endDate,
            Integer actualPeriodLengthDays
    ) {}
}