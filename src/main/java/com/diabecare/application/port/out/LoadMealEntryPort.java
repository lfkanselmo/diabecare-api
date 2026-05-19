package com.diabecare.application.port.out;

import com.diabecare.domain.model.MealEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadMealEntryPort {
    Optional<MealEntry> findById(UUID mealId);
    List<MealEntry> findByPatientIdAndDate(UUID patientId, LocalDate date);
    List<MealEntry> findByPatientIdAndDateRange(UUID patientId,
                                                LocalDate from,
                                                LocalDate to);
    void deleteById(UUID mealId);
}