package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;
import com.diabecare.infrastructure.persistence.entity.MealEntryEntity;
import com.diabecare.infrastructure.persistence.entity.MealItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {MealType.class})
public interface MealEntryPersistenceMapper {

    @Mapping(target = "id", source = "mealId")
    @Mapping(target = "mealType", expression = "java(entry.getMealType().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "items", ignore = true)
    MealEntryEntity toEntity(MealEntry entry);

    @Mapping(target = "mealId", source = "id")
    @Mapping(target = "mealType", expression = "java(MealType.valueOf(entity.getMealType()))")
    MealEntry toDomain(MealEntryEntity entity);

    @Mapping(target = "id", source = "mealItemId")
    @Mapping(target = "mealEntry", ignore = true)
    MealItemEntity toItemEntity(MealItem item);

    @Mapping(target = "mealItemId", source = "id")
    MealItem toItemDomain(MealItemEntity entity);
}