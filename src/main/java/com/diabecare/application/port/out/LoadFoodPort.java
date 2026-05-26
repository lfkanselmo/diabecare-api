package com.diabecare.application.port.out;

import com.diabecare.domain.model.Food;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadFoodPort {
    List<Food> searchByName(String query);
    List<Food> findByCategory(String category);
    Optional<Food> findById(UUID foodId);
}