package com.diabecare.application.port.out;

import com.diabecare.domain.model.MealEntry;

public interface SaveMealEntryPort {
    MealEntry save(MealEntry mealEntry);
}