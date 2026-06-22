package com.diabecare.application.port.in;

import com.diabecare.domain.model.MealEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetMealHistoryUseCase {
    List<MealEntry> getHistory(UUID patientId, LocalDate from, LocalDate to);
}