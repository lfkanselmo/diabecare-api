package com.diabecare.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface CalculateInsulinDoseUseCase {

    record Command(
            UUID patientId,
            BigDecimal currentGlucose,
            BigDecimal carbsToEat,
            boolean beforeMeal
    ) {}

    record Result(
            BigDecimal correctionDose,
            BigDecimal mealDose,
            BigDecimal totalDose,
            String explanation
    ) {}

    Result calculate(Command command);
}