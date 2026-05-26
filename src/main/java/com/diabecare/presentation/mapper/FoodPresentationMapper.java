package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.Food;
import com.diabecare.presentation.dto.response.FoodResponse;
import org.mapstruct.Mapper;

@Mapper
public interface FoodPresentationMapper {
    FoodResponse toResponse(Food food);
}