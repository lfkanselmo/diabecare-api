package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadFoodPort;
import com.diabecare.domain.model.Food;
import com.diabecare.infrastructure.persistence.mapper.FoodPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.FoodJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FoodPersistenceAdapter implements LoadFoodPort {

    private static final int SEARCH_RESULTS_LIMIT = 10;

    private final FoodJpaRepository repository;
    private final FoodPersistenceMapper mapper;

    @Override
    @Cacheable(value = "foods", key = "'search:' + #query")
    public List<Food> searchByName(String query) {
        return repository.searchByName(query, PageRequest.of(0, SEARCH_RESULTS_LIMIT))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Cacheable(value = "foods", key = "'category:' + #category")
    public List<Food> findByCategory(String category) {
        return repository.findByCategoryOrderByName(category)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Cacheable(value = "foods", key = "'id:' + #foodId")
    public Optional<Food> findById(UUID foodId) {
        return repository.findById(foodId).map(mapper::toDomain);
    }
}