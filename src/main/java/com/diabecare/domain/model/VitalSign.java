package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidVitalSignException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class VitalSign {

    private final UUID vitalId;
    private final UUID patientId;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer heartRate;
    private BigDecimal hba1c;
    private LocalDateTime measuredAt;
    private String notes;

    public static VitalSign create(
            UUID patientId,
            BigDecimal weightKg,
            BigDecimal heightCm,
            Integer systolicBp,
            Integer diastolicBp,
            Integer heartRate,
            BigDecimal hba1c,
            LocalDateTime measuredAt,
            String notes
    ) {
        validateWeight(weightKg);
        validateBloodPressure(systolicBp, diastolicBp);
        validateHeartRate(heartRate);
        validateHba1c(hba1c);

        return VitalSign.builder()
                .vitalId(UUID.randomUUID())
                .patientId(patientId)
                .weightKg(weightKg)
                .heightCm(heightCm)
                .systolicBp(systolicBp)
                .diastolicBp(diastolicBp)
                .heartRate(heartRate)
                .hba1c(hba1c)
                .measuredAt(measuredAt != null ? measuredAt : LocalDateTime.now())
                .notes(notes)
                .build();
    }

    public BigDecimal calculateBmi() {
        if (weightKg == null || heightCm == null
                || heightCm.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return weightKg.divide(heightM.multiply(heightM), 2, RoundingMode.HALF_UP);
    }

    public BmiCategory getBmiCategory() {
        BigDecimal bmi = calculateBmi();
        if (bmi == null) return null;
        if (bmi.compareTo(BigDecimal.valueOf(18.5)) < 0)  return BmiCategory.UNDERWEIGHT;
        if (bmi.compareTo(BigDecimal.valueOf(25.0)) < 0)  return BmiCategory.NORMAL;
        if (bmi.compareTo(BigDecimal.valueOf(30.0)) < 0)  return BmiCategory.OVERWEIGHT;
        return BmiCategory.OBESE;
    }

    private static void validateWeight(BigDecimal weight) {
        if (weight != null && (weight.compareTo(BigDecimal.valueOf(20)) < 0
                || weight.compareTo(BigDecimal.valueOf(500)) > 0)) {
            throw new InvalidVitalSignException("El peso debe estar entre 20 y 500 kg");
        }
    }

    private static void validateBloodPressure(Integer systolic, Integer diastolic) {
        if (systolic != null && (systolic < 50 || systolic > 300)) {
            throw new InvalidVitalSignException(
                    "La presión sistólica debe estar entre 50 y 300 mmHg");
        }
        if (diastolic != null && (diastolic < 30 || diastolic > 200)) {
            throw new InvalidVitalSignException(
                    "La presión diastólica debe estar entre 30 y 200 mmHg");
        }
    }

    private static void validateHeartRate(Integer heartRate) {
        if (heartRate != null && (heartRate < 20 || heartRate > 300)) {
            throw new InvalidVitalSignException(
                    "La frecuencia cardíaca debe estar entre 20 y 300 bpm");
        }
    }

    private static void validateHba1c(BigDecimal hba1c) {
        if (hba1c != null && (hba1c.compareTo(BigDecimal.valueOf(3)) < 0
                || hba1c.compareTo(BigDecimal.valueOf(20)) > 0)) {
            throw new InvalidVitalSignException("El valor de HbA1c debe estar entre 3% y 20%");
        }
    }
}