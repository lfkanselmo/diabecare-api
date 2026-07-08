package com.diabecare.presentation.dto.response;

import java.util.List;

public record GlucoseCorrelationResponse(
        PageResponse<GlucoseReadingResponse> readings,
        List<MealMarkerResponse> mealMarkers
) {
    public record MealMarkerResponse(
            String consumedAt,
            String mealType,
            Double totalCalories,
            Double totalCarbohydrates
    ) {}
}