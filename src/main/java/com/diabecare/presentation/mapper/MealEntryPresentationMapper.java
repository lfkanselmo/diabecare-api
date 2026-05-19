package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.presentation.dto.response.MealEntryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MealEntryPresentationMapper {

    @Mapping(target = "mealType", expression = "java(entry.getMealType().name())")
    @Mapping(target = "totalCalories", expression = "java(entry.getTotalCalories())")
    @Mapping(target = "totalCarbohydrates", expression = "java(entry.getTotalCarbohydrates())")
    @Mapping(target = "totalProteins", expression = "java(entry.getTotalProteins())")
    @Mapping(target = "totalFats", expression = "java(entry.getTotalFats())")
    MealEntryResponse toResponse(MealEntry entry);

    MealEntryResponse.MealItemResponse toItemResponse(MealItem item);
}