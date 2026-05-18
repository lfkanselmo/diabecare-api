package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidGlucoseReadingException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class GlucoseReading {

    private static final BigDecimal MIN_GLUCOSE_MG_DL = BigDecimal.valueOf(20);
    private static final BigDecimal MAX_GLUCOSE_MG_DL = BigDecimal.valueOf(600);

    private final UUID readingId;
    private final UUID patientId;
    private BigDecimal value;
    private GlucoseUnit unit;
    private ReadingType readingType;
    private LocalDateTime measuredAt;
    private String notes;
    private String deviceSource;

    public static GlucoseReading create(
            UUID patientId,
            BigDecimal value,
            GlucoseUnit unit,
            ReadingType readingType,
            LocalDateTime measuredAt,
            String notes,
            String deviceSource
    ) {
        validateValue(value, unit);
        validateMeasuredAt(measuredAt);

        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(patientId)
                .value(value)
                .unit(unit)
                .readingType(readingType)
                .measuredAt(measuredAt)
                .notes(notes)
                .deviceSource(deviceSource)
                .build();
    }

    public BigDecimal getValueInMgDl() {
        if (unit == GlucoseUnit.MG_DL) {
            return value;
        }
        return value.multiply(BigDecimal.valueOf(18.0182))
                .setScale(0, java.math.RoundingMode.HALF_UP);
    }

    public GlucoseStatus getStatus() {
        BigDecimal mgDl = getValueInMgDl();
        if (mgDl.compareTo(BigDecimal.valueOf(54)) < 0)  return GlucoseStatus.CRITICALLY_LOW;
        if (mgDl.compareTo(BigDecimal.valueOf(70)) < 0)  return GlucoseStatus.LOW;
        if (mgDl.compareTo(BigDecimal.valueOf(180)) <= 0) return GlucoseStatus.NORMAL;
        if (mgDl.compareTo(BigDecimal.valueOf(250)) <= 0) return GlucoseStatus.HIGH;
        return GlucoseStatus.CRITICALLY_HIGH;
    }

    private static void validateValue(BigDecimal value, GlucoseUnit unit) {
        if (value == null) {
            throw new InvalidGlucoseReadingException("El valor de glucosa es obligatorio");
        }
        BigDecimal mgDl = unit == GlucoseUnit.MG_DL
                ? value
                : value.multiply(BigDecimal.valueOf(18.0182));

        if (mgDl.compareTo(MIN_GLUCOSE_MG_DL) < 0 || mgDl.compareTo(MAX_GLUCOSE_MG_DL) > 0) {
            throw new InvalidGlucoseReadingException(
                    "Valor de glucosa fuera de rango válido (20–600 mg/dL)");
        }
    }

    private static void validateMeasuredAt(LocalDateTime measuredAt) {
        if (measuredAt == null) {
            throw new InvalidGlucoseReadingException("La fecha y hora de medición son obligatorias");
        }
        if (measuredAt.isAfter(LocalDateTime.now())) {
            throw new InvalidGlucoseReadingException(
                    "La fecha de medición no puede ser futura");
        }
    }
}