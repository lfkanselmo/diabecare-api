package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.CalculateInsulinDoseUseCase;
import com.diabecare.presentation.dto.request.InsulinCalculationRequest;
import com.diabecare.presentation.dto.response.InsulinCalculationResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/insulin")
@RequiredArgsConstructor
@Tag(name = "Calculadora de insulina")
public class InsulinController {

    private final CalculateInsulinDoseUseCase calculateInsulinDoseUseCase;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{patientId}/calculate")
    @Operation(
            summary = "Calcular una sugerencia educativa de dosis de insulina",
            description = "Aplica una fórmula aritmética estándar (corrección + bolo de comida) sobre los " +
                    "parámetros clínicos configurados en el perfil del paciente (factor de sensibilidad, " +
                    "ratio insulina:carbohidratos, objetivo de corrección). No es un dispositivo de dosificación " +
                    "automática ni una recomendación clínica adaptativa: los parámetros deben ser definidos por " +
                    "el equipo médico del paciente, y el resultado siempre debe validarse clínicamente antes de " +
                    "aplicarse."
    )
    public ResponseEntity<InsulinCalculationResponse> calculate(
            @PathVariable UUID patientId,
            @Valid @RequestBody InsulinCalculationRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

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
                result.explanation(),
                result.disclaimer()
        ));
    }
}