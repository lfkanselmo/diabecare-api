package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.CalculateInsulinDoseUseCase;
import com.diabecare.presentation.dto.request.InsulinCalculationRequest;
import com.diabecare.presentation.dto.response.InsulinCalculationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/insulin")
@RequiredArgsConstructor
public class InsulinController {

    private final CalculateInsulinDoseUseCase calculateInsulinDoseUseCase;

    @PostMapping("/{patientId}/calculate")
    public ResponseEntity<InsulinCalculationResponse> calculate(
            @PathVariable UUID patientId,
            @Valid @RequestBody InsulinCalculationRequest request) {

        CalculateInsulinDoseUseCase.Result result =
                calculateInsulinDoseUseCase.calculate(
                        new CalculateInsulinDoseUseCase.Command(
                                patientId,
                                request.currentGlucose(),
                                request.carbsToEat(),
                                request.beforeMeal()
                        ));

        return ResponseEntity.ok(new InsulinCalculationResponse(
                result.correctionDose(),
                result.mealDose(),
                result.totalDose(),
                result.explanation()
        ));
    }
}