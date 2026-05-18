package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidPatientDataException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Getter
@Builder
public class Patient {

    private final UUID patientId;
    private final UUID userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private DiabetesType diabetesType;
    private LocalDate diagnosisDate;
    private BigDecimal heightCm;
    private BigDecimal targetGlucoseMin;
    private BigDecimal targetGlucoseMax;
    private Integer dailyCalorieGoal;
    private ActivityLevel activityLevel;
    private GlucoseUnit preferredGlucoseUnit;

    public static Patient create(
            UUID userId,
            String fullName,
            LocalDate dateOfBirth,
            DiabetesType diabetesType,
            LocalDate diagnosisDate,
            BigDecimal heightCm
    ) {
        validateFullName(fullName);
        validateDateOfBirth(dateOfBirth);
        validateDiagnosisDate(diagnosisDate, dateOfBirth);
        validateHeight(heightCm);

        return Patient.builder()
                .patientId(UUID.randomUUID())
                .userId(userId)
                .fullName(fullName)
                .dateOfBirth(dateOfBirth)
                .diabetesType(diabetesType)
                .diagnosisDate(diagnosisDate)
                .heightCm(heightCm)
                .targetGlucoseMin(BigDecimal.valueOf(70))
                .targetGlucoseMax(BigDecimal.valueOf(180))
                .activityLevel(ActivityLevel.SEDENTARY)
                .preferredGlucoseUnit(GlucoseUnit.MG_DL)
                .build();
    }

    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean isGlucoseInRange(BigDecimal glucoseValue) {
        return glucoseValue.compareTo(targetGlucoseMin) >= 0
                && glucoseValue.compareTo(targetGlucoseMax) <= 0;
    }

    public void updateGlucoseTarget(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) >= 0) {
            throw new InvalidPatientDataException(
                    "El rango mínimo de glucosa debe ser menor que el máximo");
        }
        if (min.compareTo(BigDecimal.valueOf(50)) < 0) {
            throw new InvalidPatientDataException(
                    "El rango mínimo de glucosa no puede ser menor a 50 mg/dL");
        }
        if (max.compareTo(BigDecimal.valueOf(400)) > 0) {
            throw new InvalidPatientDataException(
                    "El rango máximo de glucosa no puede superar 400 mg/dL");
        }
        this.targetGlucoseMin = min;
        this.targetGlucoseMax = max;
    }

    public void updateDailyCalorieGoal(Integer calories) {
        if (calories != null && (calories < 500 || calories > 5000)) {
            throw new InvalidPatientDataException(
                    "La meta calórica debe estar entre 500 y 5000 kcal");
        }
        this.dailyCalorieGoal = calories;
    }

    public void updateActivityLevel(ActivityLevel level) {
        this.activityLevel = level;
    }

    public void updatePreferredGlucoseUnit(GlucoseUnit unit) {
        this.preferredGlucoseUnit = unit;
    }

    private static void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new InvalidPatientDataException("El nombre completo es obligatorio");
        }
        if (fullName.length() > 150) {
            throw new InvalidPatientDataException(
                    "El nombre no puede superar los 150 caracteres");
        }
    }

    private static void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new InvalidPatientDataException("La fecha de nacimiento es obligatoria");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new InvalidPatientDataException(
                    "La fecha de nacimiento no puede ser futura");
        }
    }

    private static void validateDiagnosisDate(LocalDate diagnosisDate,
                                              LocalDate dateOfBirth) {
        if (diagnosisDate == null) {
            throw new InvalidPatientDataException("La fecha de diagnóstico es obligatoria");
        }
        if (diagnosisDate.isBefore(dateOfBirth)) {
            throw new InvalidPatientDataException(
                    "La fecha de diagnóstico no puede ser anterior al nacimiento");
        }
        if (diagnosisDate.isAfter(LocalDate.now())) {
            throw new InvalidPatientDataException(
                    "La fecha de diagnóstico no puede ser futura");
        }
    }

    private static void validateHeight(BigDecimal heightCm) {
        if (heightCm == null) {
            throw new InvalidPatientDataException("La talla es obligatoria");
        }
        if (heightCm.compareTo(BigDecimal.valueOf(50)) < 0
                || heightCm.compareTo(BigDecimal.valueOf(250)) > 0) {
            throw new InvalidPatientDataException(
                    "La talla debe estar entre 50 y 250 cm");
        }
    }
}