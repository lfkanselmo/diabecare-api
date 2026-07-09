package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidExerciseLogException;
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
            LocalDateTime performedAt,
            BigDecimal caloriesBurnedOverride) {
        return createWithId(UUID.randomUUID(), patientId, exerciseType, intensity,
                durationMinutes, notes, performedAt, caloriesBurnedOverride);
    }

    /** Igual que {@link #create}, honrando un ID provisto por el cliente — ver {@link GlucoseReading#createWithId}. */
    public static ExerciseLog createWithId(
            UUID exerciseId,
            UUID patientId,
            ExerciseType exerciseType,
            ExerciseIntensity intensity,
            Integer durationMinutes,
            String notes,
            LocalDateTime performedAt,
            BigDecimal caloriesBurnedOverride) {

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new InvalidExerciseLogException("La duración debe ser mayor a 0 minutos");
        }
        if (performedAt != null && performedAt.isAfter(LocalDateTime.now())) {
            throw new InvalidExerciseLogException("La fecha del ejercicio no puede ser futura");
        }
        if (caloriesBurnedOverride != null &&
                (caloriesBurnedOverride.signum() < 0 ||
                        caloriesBurnedOverride.compareTo(BigDecimal.valueOf(5000)) > 0)) {
            throw new InvalidExerciseLogException(
                    "Las calorías quemadas deben estar entre 0 y 5000");
        }

        BigDecimal calories = caloriesBurnedOverride != null
                ? caloriesBurnedOverride
                : estimateCalories(exerciseType, intensity, durationMinutes);

        return ExerciseLog.builder()
                .exerciseId(exerciseId)
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
            case WALKING          -> intensity == ExerciseIntensity.HIGH ? 4.5 : 3.5;
            case RUNNING          -> intensity == ExerciseIntensity.HIGH ? 11.5 : 8.0;
            case JOGGING          -> intensity == ExerciseIntensity.HIGH ? 8.0 : 7.0;
            case CYCLING          -> intensity == ExerciseIntensity.HIGH ? 10.0 : 7.0;
            case STATIONARY_BIKE  -> intensity == ExerciseIntensity.HIGH ? 8.5 : 6.0;
            case SWIMMING         -> intensity == ExerciseIntensity.HIGH ? 9.5 : 6.0;
            case WATER_AEROBICS   -> 5.5;
            case WEIGHT_TRAINING  -> intensity == ExerciseIntensity.HIGH ? 6.0 : 4.0;
            case CALISTHENICS     -> intensity == ExerciseIntensity.HIGH ? 8.0 : 4.0;
            case CROSSFIT         -> 8.5;
            case YOGA             -> 2.5;
            case PILATES          -> 3.0;
            case STRETCHING       -> 2.3;
            case TAI_CHI          -> 3.0;
            case FOOTBALL         -> 8.0;
            case BASKETBALL       -> 7.5;
            case VOLLEYBALL       -> 4.0;
            case TENNIS           -> 7.0;
            case PADEL            -> 6.0;
            case BASEBALL         -> 5.0;
            case GOLF             -> 4.5;
            case DANCING          -> intensity == ExerciseIntensity.HIGH ? 6.0 : 4.0;
            case ZUMBA            -> 6.5;
            case AEROBICS         -> intensity == ExerciseIntensity.HIGH ? 7.5 : 5.5;
            case HIKING           -> 6.0;
            case CLIMBING         -> 7.5;
            case ELLIPTICAL       -> intensity == ExerciseIntensity.HIGH ? 8.0 : 5.5;
            case ROWING           -> intensity == ExerciseIntensity.HIGH ? 8.5 : 6.0;
            case JUMPING_ROPE     -> 10.0;
            case STAIR_CLIMBING   -> 8.0;
            case MARTIAL_ARTS     -> 10.0;
            case BOXING           -> intensity == ExerciseIntensity.HIGH ? 12.0 : 7.5;
            case SKATING          -> 7.0;
            case SKIING           -> 7.0;
            case SURFING          -> 5.0;
            case HOUSEHOLD_CHORES -> 3.0;
            case GARDENING        -> 4.0;
            case PHYSICAL_THERAPY -> 2.5;
            default               -> 4.0;
        };
        double calories = metValue * 70 * (minutes / 60.0);
        return BigDecimal.valueOf(Math.round(calories));
    }
}