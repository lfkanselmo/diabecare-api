package com.diabecare.application.port.in;

import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CyclePhase;
import com.diabecare.domain.model.MenstrualCycle;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetMenstrualCycleStatusUseCase {

    record CycleStatus(
            CyclePhase currentPhase,
            int dayOfCycle,
            boolean isOngoing,
            LocalDate periodStartDate,
            LocalDate nextCycleStart,
            String glucoseGuidance,
            Integer averageCycleLength,
            Integer averagePeriodLength,
            CycleDayEntry todayEntry,
            List<MenstrualCycle> history
    ) {}

    CycleStatus getStatus(UUID patientId);
}