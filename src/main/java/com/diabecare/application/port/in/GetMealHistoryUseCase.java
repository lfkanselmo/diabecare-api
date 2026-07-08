package com.diabecare.application.port.in;

import com.diabecare.domain.model.MealEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface GetMealHistoryUseCase {
    Page<MealEntry> getHistory(UUID patientId, LocalDate from, LocalDate to, Pageable pageable);
}