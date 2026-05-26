package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SearchFoodUseCase;
import com.diabecare.application.port.out.LoadFoodPort;
import com.diabecare.domain.model.Food;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchFoodUseCaseImpl implements SearchFoodUseCase {

    private final LoadFoodPort loadFoodPort;

    @Override
    public List<Food> search(String query) {
        if (query == null || query.isBlank() || query.length() < 2) return List.of();
        return loadFoodPort.searchByName(query.trim());
    }

    @Override
    public List<Food> findByCategory(String category) {
        return loadFoodPort.findByCategory(category);
    }
}