package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID patientId,
        String fullName,
        LocalDate dateOfBirth,
        int age,
        String diabetesType,
        LocalDate diagnosisDate,
        BigDecimal heightCm,
        BigDecimal targetGlucoseMin,
        BigDecimal targetGlucoseMax,
        Integer dailyCalorieGoal,
        String activityLevel,
        String preferredGlucoseUnit
) {}