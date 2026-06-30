package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidVitalSignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("VitalSign")
class VitalSignTest {

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("crea un registro válido con todos los campos")
        void createsValidVitalSign() {
            VitalSign vital = VitalSign.create(
                    patientId, BigDecimal.valueOf(70), BigDecimal.valueOf(170),
                    120, 80, 70, BigDecimal.valueOf(6.5), LocalDateTime.now().minusMinutes(5), "nota");

            assertThat(vital.getVitalId()).isNotNull();
            assertThat(vital.getWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(70));
            assertThat(vital.getSystolicBp()).isEqualTo(120);
        }

        @Test
        @DisplayName("crea un registro válido con todos los campos opcionales en null")
        void createsValidVitalSignWithAllOptionalNull() {
            assertThatCode(() -> VitalSign.create(
                    patientId, null, null, null, null, null, null, null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("usa la fecha actual cuando measuredAt es nulo")
        void usesNowWhenMeasuredAtIsNull() {
            LocalDateTime before = LocalDateTime.now();
            VitalSign vital = VitalSign.create(
                    patientId, BigDecimal.valueOf(70), null, null, null, null, null, null, null);
            LocalDateTime after = LocalDateTime.now();

            assertThat(vital.getMeasuredAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("rechaza peso por debajo de 20 kg")
        void rejectsWeightBelow20() {
            assertThatThrownBy(() -> VitalSign.create(
                    patientId, BigDecimal.valueOf(19), null, null, null, null, null, null, null))
                    .isInstanceOf(InvalidVitalSignException.class)
                    .hasMessageContaining("20 y 500");
        }

        @Test
        @DisplayName("rechaza peso por encima de 500 kg")
        void rejectsWeightAbove500() {
            assertThatThrownBy(() -> VitalSign.create(
                    patientId, BigDecimal.valueOf(501), null, null, null, null, null, null, null))
                    .isInstanceOf(InvalidVitalSignException.class);
        }

        @Test
        @DisplayName("acepta los límites exactos de peso (20 y 500 kg)")
        void acceptsExactWeightBoundaries() {
            assertThatCode(() -> VitalSign.create(
                    patientId, BigDecimal.valueOf(20), null, null, null, null, null, null, null))
                    .doesNotThrowAnyException();
            assertThatCode(() -> VitalSign.create(
                    patientId, BigDecimal.valueOf(500), null, null, null, null, null, null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechaza presión sistólica fuera de rango")
        void rejectsSystolicOutOfRange() {
            assertThatThrownBy(() -> VitalSign.create(
                    patientId, null, null, 49, 80, null, null, null, null))
                    .isInstanceOf(InvalidVitalSignException.class)
                    .hasMessageContaining("sistólica");

            assertThatThrownBy(() -> VitalSign.create(
                    patientId, null, null, 301, 80, null, null, null, null))
                    .isInstanceOf(InvalidVitalSignException.class);
        }

        @Test
        @DisplayName("rechaza presión diastólica fuera de rango")
        void rejectsDiastolicOutOfRange() {
            assertThatThrownBy(() -> VitalSign.create(
                    patientId, null, null, 120, 29, null, null, null, null))
                    .isInstanceOf(InvalidVitalSignException.class)
                    .hasMessageContaining("diastólica");

            assertThatThrownBy(() -> VitalSign.create(
                    patientId, null, null, 120, 201, null, null, null, null))
                    .isInstanceOf(InvalidVitalSignException.class);
        }

        @Test
        @DisplayName("rechaza frecuencia cardíaca fuera de rango")
        void rejectsHeartRateOutOfRange() {
            assertThatThrownBy(() -> VitalSign.create(
                    patientId, null, null, null, null, 19, null, null, null))
                    .isInstanceOf(InvalidVitalSignException.class)
                    .hasMessageContaining("cardíaca");

            assertThatThrownBy(() -> VitalSign.create(
                    patientId, null, null, null, null, 301, null, null, null))
                    .isInstanceOf(InvalidVitalSignException.class);
        }

        @Test
        @DisplayName("rechaza HbA1c fuera de rango")
        void rejectsHba1cOutOfRange() {
            assertThatThrownBy(() -> VitalSign.create(
                    patientId, null, null, null, null, null, BigDecimal.valueOf(2.9), null, null))
                    .isInstanceOf(InvalidVitalSignException.class)
                    .hasMessageContaining("HbA1c");

            assertThatThrownBy(() -> VitalSign.create(
                    patientId, null, null, null, null, null, BigDecimal.valueOf(20.1), null, null))
                    .isInstanceOf(InvalidVitalSignException.class);
        }
    }

    @Nested
    @DisplayName("calculateBmi")
    class CalculateBmi {

        @Test
        @DisplayName("calcula el IMC correctamente")
        void calculatesBmiCorrectly() {
            VitalSign vital = vitalWith(BigDecimal.valueOf(70), BigDecimal.valueOf(170));
            // 70 / (1.70^2) = 24.22
            assertThat(vital.calculateBmi()).isEqualByComparingTo(BigDecimal.valueOf(24.22));
        }

        @Test
        @DisplayName("retorna null cuando falta el peso")
        void returnsNullWhenWeightMissing() {
            VitalSign vital = vitalWith(null, BigDecimal.valueOf(170));
            assertThat(vital.calculateBmi()).isNull();
        }

        @Test
        @DisplayName("retorna null cuando falta la talla")
        void returnsNullWhenHeightMissing() {
            VitalSign vital = vitalWith(BigDecimal.valueOf(70), null);
            assertThat(vital.calculateBmi()).isNull();
        }

        @Test
        @DisplayName("retorna null cuando la talla es cero, evitando división por cero")
        void returnsNullWhenHeightIsZero() {
            VitalSign vital = vitalWith(BigDecimal.valueOf(70), BigDecimal.ZERO);
            assertThat(vital.calculateBmi()).isNull();
        }
    }

    @Nested
    @DisplayName("getBmiCategory")
    class GetBmiCategory {

        @ParameterizedTest(name = "peso={0}kg, talla={1}cm -> {2}")
        @DisplayName("clasifica correctamente cada categoría de IMC")
        @CsvSource({
                "50, 180, UNDERWEIGHT",
                "70, 180, NORMAL",
                "85, 180, OVERWEIGHT",
                "100, 180, OBESE"
        })
        void classifiesEachCategory(double weight, double height, BmiCategory expected) {
            VitalSign vital = vitalWith(BigDecimal.valueOf(weight), BigDecimal.valueOf(height));
            assertThat(vital.getBmiCategory()).isEqualTo(expected);
        }

        @Test
        @DisplayName("retorna null cuando no se puede calcular el IMC")
        void returnsNullWhenBmiCannotBeCalculated() {
            VitalSign vital = vitalWith(null, null);
            assertThat(vital.getBmiCategory()).isNull();
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private VitalSign vitalWith(BigDecimal weightKg, BigDecimal heightCm) {
        return VitalSign.builder()
                .vitalId(UUID.randomUUID())
                .patientId(patientId)
                .weightKg(weightKg)
                .heightCm(heightCm)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}