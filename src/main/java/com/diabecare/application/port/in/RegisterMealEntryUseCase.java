package com.diabecare.application.port.in;

import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RegisterMealEntryUseCase {

    record Command(
            UUID patientId,
            MealType mealType,
            LocalDateTime consumedAt,
            String notes,
            List<MealItem> items
    ) {}

    MealEntry execute(Command command);
}