package com.diabecare.application.port.in;

import com.diabecare.domain.model.CyclePhase;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetCyclePhaseCalendarUseCase {

    record DayPhase(LocalDate date, CyclePhase phase) {}

    List<DayPhase> getCalendar(UUID patientId, LocalDate from, LocalDate to);
}