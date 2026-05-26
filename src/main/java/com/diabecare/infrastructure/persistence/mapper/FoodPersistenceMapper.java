package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.Food;
import com.diabecare.infrastructure.persistence.entity.FoodEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FoodPersistenceMapper {

    @Mapping(target = "foodId", source = "id")
    Food toDomain(FoodEntity entity);
}