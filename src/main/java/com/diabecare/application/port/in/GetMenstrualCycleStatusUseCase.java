package com.diabecare.application.port.in;

import com.diabecare.domain.model.CyclePhase;
import com.diabecare.domain.model.MenstrualCycle;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetMenstrualCycleStatusUseCase {

    record CycleStatus(
            CyclePhase currentPhase,
            int dayOfCycle,
            LocalDate nextCycleStart,
            String glucoseGuidance,
            double averageCycleLength,
            List<MenstrualCycle> history
    ) {}

    CycleStatus getStatus(UUID patientId);
}