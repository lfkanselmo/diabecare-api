package com.diabecare.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MenstrualCycleStatusResponse(
        String currentPhase,
        String currentPhaseLabel,
        int dayOfCycle,
        LocalDate nextCycleStart,
        int daysUntilNextCycle,
        String glucoseGuidance,
        double averageCycleLength,
        List<CycleHistoryItem> history
) {
    public record CycleHistoryItem(
            String cycleId,
            LocalDate startDate,
            Integer cycleLengthDays,
            Integer periodLengthDays,
            String symptoms
    ) {}
}