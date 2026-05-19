package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetDailySummaryUseCase;
import com.diabecare.application.port.in.RegisterMealEntryUseCase;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;
import com.diabecare.presentation.dto.request.RegisterMealRequest;
import com.diabecare.presentation.dto.response.DailySummaryResponse;
import com.diabecare.presentation.dto.response.MealEntryResponse;
import com.diabecare.presentation.mapper.DailySummaryPresentationMapper;
import com.diabecare.presentation.mapper.MealEntryPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final RegisterMealEntryUseCase registerMealEntryUseCase;
    private final GetDailySummaryUseCase getDailySummaryUseCase;
    private final MealEntryPresentationMapper mealMapper;
    private final DailySummaryPresentationMapper summaryMapper;

    @PostMapping("/{patientId}/meals")
    public ResponseEntity<MealEntryResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody RegisterMealRequest request) {

        List<MealItem> items = request.items().stream()
                .map(i -> MealItem.create(i.foodName(), i.quantityGrams(),
                        i.calories(), i.carbohydrates(), i.proteins(), i.fats(), i.foodCode()))
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                mealMapper.toResponse(registerMealEntryUseCase.execute(
                        new RegisterMealEntryUseCase.Command(
                                patientId,
                                MealType.valueOf(request.mealType()),
                                request.consumedAt(),
                                request.notes(),
                                items
                        ))));
    }

    @GetMapping("/{patientId}/summary")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(summaryMapper.toResponse(
                getDailySummaryUseCase.getSummary(patientId, date)));
    }
}