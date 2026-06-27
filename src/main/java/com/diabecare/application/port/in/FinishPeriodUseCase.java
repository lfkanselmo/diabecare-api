package com.diabecare.application.port.in;

import com.diabecare.domain.model.MenstrualCycle;

import java.time.LocalDate;
import java.util.UUID;

public interface FinishPeriodUseCase {

    record Command(UUID patientId, LocalDate endDate) {}

    MenstrualCycle execute(Command command);
}