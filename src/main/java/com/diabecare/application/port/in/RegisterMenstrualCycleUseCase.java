package com.diabecare.application.port.in;

import com.diabecare.domain.model.MenstrualCycle;

import java.time.LocalDate;
import java.util.UUID;

public interface RegisterMenstrualCycleUseCase {

    record Command(
            UUID patientId,
            LocalDate startDate,
            Integer periodLengthDays,
            String symptoms,
            String notes
    ) {}

    MenstrualCycle execute(Command command);
}