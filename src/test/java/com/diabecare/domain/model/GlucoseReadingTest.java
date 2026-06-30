package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidGlucoseReadingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GlucoseReading")
class GlucoseReadingTest {

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("crea una lectura válida con todos los campos")
        void createsValidReading() {
            GlucoseReading reading = GlucoseReading.create(
                    patientId, BigDecimal.valueOf(100), GlucoseUnit.MG_DL,
                    ReadingType.FASTING, LocalDateTime.now().minusMinutes(5),
                    "nota", "glucómetro X");

            assertThat(reading.getReadingId()).isNotNull();
            assertThat(reading.getPatientId()).isEqualTo(patientId);
            assertThat(reading.getValue()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(reading.getUnit()).isEqualTo(GlucoseUnit.MG_DL);
            assertThat(reading.getNotes()).isEqualTo("nota");
            assertThat(reading.getDeviceSource()).isEqualTo("glucómetro X");
        }

        @Test
        @DisplayName("rechaza valor nulo")
        void rejectsNullValue() {
            assertThatThrownBy(() -> GlucoseReading.create(
                    patientId, null, GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now(), null, null))
                    .isInstanceOf(InvalidGlucoseReadingException.class)
                    .hasMessageContaining("obligatorio");
        }

        @Test
        @DisplayName("acepta el límite mínimo exacto (20 mg/dL)")
        void acceptsExactMinimum() {
            assertThatCode(() -> GlucoseReading.create(
                    patientId, BigDecimal.valueOf(20), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now(), null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("acepta el límite máximo exacto (600 mg/dL)")
        void acceptsExactMaximum() {
            assertThatCode(() -> GlucoseReading.create(
                    patientId, BigDecimal.valueOf(600), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now(), null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechaza valor justo por debajo del mínimo (19 mg/dL)")
        void rejectsJustBelowMinimum() {
            assertThatThrownBy(() -> GlucoseReading.create(
                    patientId, BigDecimal.valueOf(19), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now(), null, null))
                    .isInstanceOf(InvalidGlucoseReadingException.class)
                    .hasMessageContaining("fuera de rango");
        }

        @Test
        @DisplayName("rechaza valor justo por encima del máximo (601 mg/dL)")
        void rejectsJustAboveMaximum() {
            assertThatThrownBy(() -> GlucoseReading.create(
                    patientId, BigDecimal.valueOf(601), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now(), null, null))
                    .isInstanceOf(InvalidGlucoseReadingException.class)
                    .hasMessageContaining("fuera de rango");
        }

        @Test
        @DisplayName("valida el rango también cuando la unidad es mmol/L")
        void validatesRangeInMmolL() {
            // 2 mmol/L equivale a ~36 mg/dL, dentro de rango
            assertThatCode(() -> GlucoseReading.create(
                    patientId, BigDecimal.valueOf(2), GlucoseUnit.MMOL_L,
                    ReadingType.RANDOM, LocalDateTime.now(), null, null))
                    .doesNotThrowAnyException();

            // 0.5 mmol/L equivale a ~9 mg/dL, fuera de rango
            assertThatThrownBy(() -> GlucoseReading.create(
                    patientId, BigDecimal.valueOf(0.5), GlucoseUnit.MMOL_L,
                    ReadingType.RANDOM, LocalDateTime.now(), null, null))
                    .isInstanceOf(InvalidGlucoseReadingException.class);
        }

        @Test
        @DisplayName("rechaza fecha de medición nula")
        void rejectsNullMeasuredAt() {
            assertThatThrownBy(() -> GlucoseReading.create(
                    patientId, BigDecimal.valueOf(100), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, null, null, null))
                    .isInstanceOf(InvalidGlucoseReadingException.class)
                    .hasMessageContaining("obligatorias");
        }

        @Test
        @DisplayName("rechaza fecha de medición futura")
        void rejectsFutureMeasuredAt() {
            assertThatThrownBy(() -> GlucoseReading.create(
                    patientId, BigDecimal.valueOf(100), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now().plusMinutes(5), null, null))
                    .isInstanceOf(InvalidGlucoseReadingException.class)
                    .hasMessageContaining("futura");
        }
    }

    @Nested
    @DisplayName("getValueInMgDl")
    class GetValueInMgDl {

        @Test
        @DisplayName("retorna el mismo valor cuando la unidad ya es mg/dL")
        void returnsSameValueWhenAlreadyMgDl() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(120), GlucoseUnit.MG_DL);
            assertThat(reading.getValueInMgDl()).isEqualByComparingTo(BigDecimal.valueOf(120));
        }

        @Test
        @DisplayName("convierte mmol/L a mg/dL correctamente")
        void convertsMmolLToMgDl() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(6), GlucoseUnit.MMOL_L);
            // 6 * 18.0182 = 108.1092 -> redondeado a 108
            assertThat(reading.getValueInMgDl()).isEqualByComparingTo(BigDecimal.valueOf(108));
        }
    }

    @Nested
    @DisplayName("getStatus")
    class GetStatus {

        @ParameterizedTest(name = "{0} mg/dL -> {1}")
        @DisplayName("clasifica correctamente cada rango de glucosa")
        @CsvSource({
                "53, CRITICALLY_LOW",
                "54, LOW",
                "69, LOW",
                "70, NORMAL",
                "180, NORMAL",
                "181, HIGH",
                "250, HIGH",
                "251, CRITICALLY_HIGH"
        })
        void classifiesEachRange(int value, GlucoseStatus expectedStatus) {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(value), GlucoseUnit.MG_DL);
            assertThat(reading.getStatus()).isEqualTo(expectedStatus);
        }

        @Test
        @DisplayName("clasifica usando la conversión cuando la unidad es mmol/L")
        void classifiesUsingConvertedValueForMmolL() {
            // 10 mmol/L = ~180 mg/dL -> NORMAL (límite superior incluido)
            GlucoseReading reading = readingWith(BigDecimal.valueOf(10), GlucoseUnit.MMOL_L);
            assertThat(reading.getStatus()).isEqualTo(GlucoseStatus.NORMAL);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private GlucoseReading readingWith(BigDecimal value, GlucoseUnit unit) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(patientId)
                .value(value)
                .unit(unit)
                .readingType(ReadingType.RANDOM)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}