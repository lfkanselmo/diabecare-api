package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetDailySummaryUseCase;
import com.diabecare.application.port.in.GetMealHistoryUseCase;
import com.diabecare.application.port.in.RegisterMealEntryUseCase;
import com.diabecare.application.port.in.SyncMealEntriesUseCase;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;
import com.diabecare.presentation.dto.request.RegisterMealRequest;
import com.diabecare.presentation.dto.response.DailySummaryResponse;
import com.diabecare.presentation.dto.response.MealEntryResponse;
import com.diabecare.presentation.dto.response.PageResponse;
import com.diabecare.presentation.mapper.DailySummaryPresentationMapper;
import com.diabecare.presentation.mapper.MealEntryPresentationMapper;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final RegisterMealEntryUseCase registerMealEntryUseCase;
    private final GetDailySummaryUseCase getDailySummaryUseCase;
    private final GetMealHistoryUseCase getMealHistoryUseCase;
    private final SyncMealEntriesUseCase syncMealEntriesUseCase;
    private final MealEntryPresentationMapper mealMapper;
    private final DailySummaryPresentationMapper summaryMapper;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{patientId}/meals")
    public ResponseEntity<MealEntryResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody RegisterMealRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        List<MealItem> items = request.items().stream()
                .map(i -> i.mealItemId() != null
                        ? MealItem.createWithId(i.mealItemId(), i.foodName(), i.quantityGrams(),
                                i.calories(), i.carbohydrates(), i.proteins(), i.fats(), i.foodCode())
                        : MealItem.create(i.foodName(), i.quantityGrams(),
                                i.calories(), i.carbohydrates(), i.proteins(), i.fats(), i.foodCode()))
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                mealMapper.toResponse(registerMealEntryUseCase.execute(
                        new RegisterMealEntryUseCase.Command(
                                patientId,
                                MealType.valueOf(request.mealType()),
                                request.consumedAt(),
                                request.notes(),
                                items,
                                request.mealId()
                        ))));
    }

    @GetMapping("/{patientId}/summary")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(summaryMapper.toResponse(
                getDailySummaryUseCase.getSummary(patientId, date)));
    }

    @GetMapping("/{patientId}/meals")
    public ResponseEntity<PageResponse<MealEntryResponse>> getHistory(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(PageResponse.of(
                getMealHistoryUseCase.getHistory(patientId, from, to, PageRequest.of(page, size)),
                mealMapper::toResponse));
    }

    /**
     * Cursor de sincronización incremental para el motor offline-first del móvil —
     * distinto de /meals (que pagina por fecha de consumo para la UI web).
     * {@code since} ausente trae el historial completo (primera sincronización).
     */
    @GetMapping("/{patientId}/meals/sync")
    public ResponseEntity<List<MealEntryResponse>> sync(
            @PathVariable UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        List<MealEntryResponse> meals = syncMealEntriesUseCase.execute(patientId, since).stream()
                .map(mealMapper::toResponse)
                .toList();

        return ResponseEntity.ok(meals);
    }
}