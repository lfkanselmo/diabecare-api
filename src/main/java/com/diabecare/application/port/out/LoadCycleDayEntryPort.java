package com.diabecare.application.port.out;

import com.diabecare.domain.model.CycleDayEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadCycleDayEntryPort {
    Optional<CycleDayEntry> findByCycleIdAndDate(UUID cycleId, LocalDate date);
    List<CycleDayEntry> findByCycleId(UUID cycleId);
    List<CycleDayEntry> findByPatientIdAndDateRange(UUID patientId, LocalDate from, LocalDate to);
}