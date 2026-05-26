package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadFoodPort;
import com.diabecare.domain.model.Food;
import com.diabecare.infrastructure.persistence.mapper.FoodPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.FoodJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FoodPersistenceAdapter implements LoadFoodPort {

    private final FoodJpaRepository repository;
    private final FoodPersistenceMapper mapper;

    @Override
    public List<Food> searchByName(String query) {
        return repository.searchByName(query)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Food> findByCategory(String category) {
        return repository.findByCategoryOrderByName(category)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Food> findById(UUID foodId) {
        return repository.findById(foodId).map(mapper::toDomain);
    }
}