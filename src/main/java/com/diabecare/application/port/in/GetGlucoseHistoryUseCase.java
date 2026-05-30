package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.MealEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GetGlucoseHistoryUseCase {

    record Result(
            List<GlucoseReading> readings,
            List<MealEntry> mealEntries
    ) {}

    Result getByPatientAndDateRange(UUID patientId, LocalDateTime from, LocalDateTime to);
}