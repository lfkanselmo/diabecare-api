package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidExerciseLogException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExerciseLog")
class ExerciseLogTest {

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("create — validaciones")
    class Validations {

        @Test
        @DisplayName("rechaza duración nula")
        void rejectsNullDuration() {
            assertThatThrownBy(() -> ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    null, null, null, null))
                    .isInstanceOf(InvalidExerciseLogException.class)
                    .hasMessageContaining("duración");
        }

        @Test
        @DisplayName("rechaza duración igual o menor a cero")
        void rejectsZeroOrNegativeDuration() {
            assertThatThrownBy(() -> ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    0, null, null, null))
                    .isInstanceOf(InvalidExerciseLogException.class);

            assertThatThrownBy(() -> ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    -10, null, null, null))
                    .isInstanceOf(InvalidExerciseLogException.class);
        }

        @Test
        @DisplayName("rechaza fecha futura")
        void rejectsFuturePerformedAt() {
            assertThatThrownBy(() -> ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    30, null, LocalDateTime.now().plusHours(1), null))
                    .isInstanceOf(InvalidExerciseLogException.class)
                    .hasMessageContaining("futura");
        }

        @Test
        @DisplayName("usa la fecha actual cuando performedAt es nulo")
        void usesNowWhenPerformedAtIsNull() {
            LocalDateTime before = LocalDateTime.now();
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE, 30, null, null, null);
            assertThat(log.getPerformedAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("rechaza override de calorías negativo")
        void rejectsNegativeCaloriesOverride() {
            assertThatThrownBy(() -> ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    30, null, null, BigDecimal.valueOf(-1)))
                    .isInstanceOf(InvalidExerciseLogException.class)
                    .hasMessageContaining("entre 0 y 5000");
        }

        @Test
        @DisplayName("rechaza override de calorías por encima de 5000")
        void rejectsCaloriesOverrideAbove5000() {
            assertThatThrownBy(() -> ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    30, null, null, BigDecimal.valueOf(5001)))
                    .isInstanceOf(InvalidExerciseLogException.class);
        }

        @Test
        @DisplayName("acepta los límites exactos del override (0 y 5000)")
        void acceptsExactOverrideBoundaries() {
            assertThatCode(() -> ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    30, null, null, BigDecimal.ZERO))
                    .doesNotThrowAnyException();
            assertThatCode(() -> ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    30, null, null, BigDecimal.valueOf(5000)))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("create — estimación de calorías")
    class CaloriesEstimation {

        @Test
        @DisplayName("usa el override cuando se especifica, ignorando el cálculo automático")
        void usesOverrideWhenProvided() {
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.LOW,
                    30, null, null, BigDecimal.valueOf(999));

            assertThat(log.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(999));
        }

        @Test
        @DisplayName("estima calorías automáticamente cuando no hay override")
        void estimatesCaloriesWhenNoOverride() {
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    60, null, null, null);

            // MET 3.5 * 70 * 1.0 = 245
            assertThat(log.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(245));
        }

        @Test
        @DisplayName("diferencia el cálculo según la intensidad para tipos que lo soportan")
        void differentiatesCaloriesByIntensity() {
            ExerciseLog moderate = ExerciseLog.create(
                    patientId, ExerciseType.RUNNING, ExerciseIntensity.MODERATE, 60, null, null, null);
            ExerciseLog high = ExerciseLog.create(
                    patientId, ExerciseType.RUNNING, ExerciseIntensity.HIGH, 60, null, null, null);

            assertThat(high.getCaloriesBurned()).isGreaterThan(moderate.getCaloriesBurned());
        }

        @Test
        @DisplayName("usa MET genérico (4.0) para el tipo OTHER")
        void usesGenericMetForOther() {
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.OTHER, ExerciseIntensity.MODERATE, 60, null, null, null);

            // MET 4.0 * 70 * 1.0 = 280
            assertThat(log.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(280));
        }

        @Test
        @DisplayName("escala proporcionalmente con la duración")
        void scalesWithDuration() {
            ExerciseLog thirtyMin = ExerciseLog.create(
                    patientId, ExerciseType.YOGA, ExerciseIntensity.LOW, 30, null, null, null);
            ExerciseLog sixtyMin = ExerciseLog.create(
                    patientId, ExerciseType.YOGA, ExerciseIntensity.LOW, 60, null, null, null);

            assertThat(sixtyMin.getCaloriesBurned().doubleValue())
                    .isCloseTo(thirtyMin.getCaloriesBurned().doubleValue() * 2, within(1.0));
        }
    }
}