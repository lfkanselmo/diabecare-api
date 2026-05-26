package com.diabecare.application.port.in;

import com.diabecare.domain.model.Food;

import java.util.List;

public interface SearchFoodUseCase {
    List<Food> search(String query);
    List<Food> findByCategory(String category);
}