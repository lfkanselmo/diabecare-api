package com.diabecare.domain.model;

import com.diabecare.domain.exception.DomainException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ExerciseLog {

    private final UUID exerciseId;
    private final UUID patientId;
    private ExerciseType exerciseType;
    private ExerciseIntensity intensity;
    private Integer durationMinutes;
    private BigDecimal caloriesBurned;
    private String notes;
    private LocalDateTime performedAt;

    public static ExerciseLog create(
            UUID patientId,
            ExerciseType exerciseType,
            ExerciseIntensity intensity,
            Integer durationMinutes,
            String notes,
            LocalDateTime performedAt) {

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new DomainException("La duración debe ser mayor a 0 minutos") {};
        }
        if (performedAt != null && performedAt.isAfter(LocalDateTime.now())) {
            throw new DomainException("La fecha del ejercicio no puede ser futura") {};
        }

        BigDecimal calories = estimateCalories(exerciseType, intensity, durationMinutes);

        return ExerciseLog.builder()
                .exerciseId(UUID.randomUUID())
                .patientId(patientId)
                .exerciseType(exerciseType)
                .intensity(intensity)
                .durationMinutes(durationMinutes)
                .caloriesBurned(calories)
                .notes(notes)
                .performedAt(performedAt != null ? performedAt : LocalDateTime.now())
                .build();
    }

    private static BigDecimal estimateCalories(ExerciseType type,
                                               ExerciseIntensity intensity,
                                               int minutes) {
        double metValue = switch (type) {
            case WALKING         -> intensity == ExerciseIntensity.HIGH ? 4.5 : 3.5;
            case RUNNING         -> intensity == ExerciseIntensity.HIGH ? 11.0 : 8.0;
            case CYCLING         -> intensity == ExerciseIntensity.HIGH ? 10.0 : 7.0;
            case SWIMMING        -> intensity == ExerciseIntensity.HIGH ? 9.0 : 6.0;
            case WEIGHT_TRAINING -> intensity == ExerciseIntensity.HIGH ? 6.0 : 4.0;
            case YOGA            -> 2.5;
            case FOOTBALL        -> 8.0;
            case BASKETBALL      -> 7.5;
            case DANCING         -> intensity == ExerciseIntensity.HIGH ? 6.0 : 4.0;
            case HIKING          -> 6.0;
            default              -> 4.0;
        };
        double calories = metValue * 70 * (minutes / 60.0);
        return BigDecimal.valueOf(Math.round(calories));
    }
}