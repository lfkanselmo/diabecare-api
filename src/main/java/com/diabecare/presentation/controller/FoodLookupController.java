package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.LookupFoodByBarcodeUseCase;
import com.diabecare.domain.model.ExternalFoodInfo;
import com.diabecare.presentation.dto.response.ExternalFoodResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/food-lookup")
@RequiredArgsConstructor
@Tag(name = "Búsqueda de alimentos por código de barras")
public class FoodLookupController {

    private final LookupFoodByBarcodeUseCase lookupFoodByBarcodeUseCase;

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ExternalFoodResponse> lookupByBarcode(@PathVariable String barcode) {
        return lookupFoodByBarcodeUseCase.execute(barcode)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ExternalFoodResponse toResponse(ExternalFoodInfo info) {
        return new ExternalFoodResponse(
                info.getBarcode(), info.getName(), info.getBrand(),
                info.getCaloriesPer100g(), info.getCarbsPer100g(),
                info.getProteinsPer100g(), info.getFatsPer100g());
    }
}
