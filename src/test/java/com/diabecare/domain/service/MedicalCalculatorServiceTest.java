package com.diabecare.domain.service;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MedicalCalculatorService")
class MedicalCalculatorServiceTest {

    private MedicalCalculatorService calculator;

    @BeforeEach
    void setUp() {
        calculator = new MedicalCalculatorService();
    }

    @Nested
    @DisplayName("calculateAverage")
    class CalculateAverage {

        @Test
        @DisplayName("retorna cero con lista vacía")
        void returnsZeroWhenEmpty() {
            BigDecimal result = calculator.calculateAverage(List.of());
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("calcula correctamente con múltiples lecturas")
        void calculatesCorrectly() {
            List<GlucoseReading> readings = List.of(
                    reading(100), reading(200), reading(300)
            );
            BigDecimal result = calculator.calculateAverage(readings);
            assertThat(result).isEqualByComparingTo(new BigDecimal("200.0"));
        }

        @Test
        @DisplayName("calcula con una sola lectura")
        void calculatesSingleReading() {
            BigDecimal result = calculator.calculateAverage(List.of(reading(150)));
            assertThat(result).isEqualByComparingTo(new BigDecimal("150.0"));
        }
    }

    @Nested
    @DisplayName("estimateHba1c")
    class EstimateHba1c {

        @Test
        @DisplayName("estima HbA1c usando fórmula ADAG correctamente")
        void estimatesCorrectly() {
            // (154 + 46.7) / 28.7 = 7.0 (promedio de referencia ADA)
            BigDecimal hba1c = calculator.estimateHba1c(new BigDecimal("154"));
            assertThat(hba1c.doubleValue()).isCloseTo(7.0, within(0.1));
        }

        @Test
        @DisplayName("glucosa alta resulta en HbA1c alta")
        void highGlucoseHighHba1c() {
            BigDecimal hba1c = calculator.estimateHba1c(new BigDecimal("250"));
            assertThat(hba1c.doubleValue()).isGreaterThan(9.0);
        }

        @Test
        @DisplayName("glucosa normal resulta en HbA1c normal")
        void normalGlucoseNormalHba1c() {
            BigDecimal hba1c = calculator.estimateHba1c(new BigDecimal("100"));
            assertThat(hba1c.doubleValue()).isLessThan(6.5);
        }
    }

    @Nested
    @DisplayName("calculateTimeInRange")
    class CalculateTimeInRange {

        @Test
        @DisplayName("retorna 100% cuando todas las lecturas están en rango")
        void returns100WhenAllInRange() {
            List<GlucoseReading> readings = List.of(
                    reading(80), reading(100), reading(150), reading(170)
            );
            BigDecimal tir = calculator.calculateTimeInRange(
                    readings, new BigDecimal("70"), new BigDecimal("180"));
            assertThat(tir).isEqualByComparingTo(new BigDecimal("100.0"));
        }

        @Test
        @DisplayName("retorna 0% cuando ninguna lectura está en rango")
        void returns0WhenNoneInRange() {
            List<GlucoseReading> readings = List.of(
                    reading(50), reading(55), reading(250), reading(300)
            );
            BigDecimal tir = calculator.calculateTimeInRange(
                    readings, new BigDecimal("70"), new BigDecimal("180"));
            assertThat(tir).isEqualByComparingTo(new BigDecimal("0.0"));
        }

        @Test
        @DisplayName("calcula correctamente con mezcla de lecturas")
        void calculatesMixedReadings() {
            List<GlucoseReading> readings = List.of(
                    reading(80),  // en rango
                    reading(150), // en rango
                    reading(50),  // fuera de rango
                    reading(250)  // fuera de rango
            );
            BigDecimal tir = calculator.calculateTimeInRange(
                    readings, new BigDecimal("70"), new BigDecimal("180"));
            assertThat(tir).isEqualByComparingTo(new BigDecimal("50.0"));
        }

        @Test
        @DisplayName("retorna 0 con lista vacía")
        void returnsZeroWhenEmpty() {
            BigDecimal tir = calculator.calculateTimeInRange(
                    List.of(), new BigDecimal("70"), new BigDecimal("180"));
            assertThat(tir).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("calculateCoefficientOfVariation")
    class CalculateCoefficientOfVariation {

        @Test
        @DisplayName("calcula CV correctamente")
        void calculatesCorrectly() {
            BigDecimal stdDev  = new BigDecimal("30");
            BigDecimal average = new BigDecimal("150");
            BigDecimal cv = calculator.calculateCoefficientOfVariation(stdDev, average);
            assertThat(cv.doubleValue()).isCloseTo(20.0, within(0.1));
        }

        @Test
        @DisplayName("retorna 0 cuando el promedio es 0")
        void returnsZeroWhenAverageIsZero() {
            BigDecimal cv = calculator.calculateCoefficientOfVariation(
                    new BigDecimal("30"), BigDecimal.ZERO);
            assertThat(cv).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("calculateTirDetailed")
    class CalculateTirDetailed {

        @Test
        @DisplayName("clasifica correctamente los 5 rangos")
        void classifiesCorrectly() {
            List<GlucoseReading> readings = List.of(
                    reading(40),  // muy bajo
                    reading(60),  // bajo
                    reading(100), // en rango
                    reading(200), // alto
                    reading(300)  // muy alto
            );
            var result = calculator.calculateTirDetailed(readings);
            assertThat(result.get("veryLow").doubleValue()).isEqualTo(20.0);
            assertThat(result.get("low").doubleValue()).isEqualTo(20.0);
            assertThat(result.get("inRange").doubleValue()).isEqualTo(20.0);
            assertThat(result.get("high").doubleValue()).isEqualTo(20.0);
            assertThat(result.get("veryHigh").doubleValue()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("retorna mapa vacío con lista vacía")
        void returnsEmptyWhenEmpty() {
            assertThat(calculator.calculateTirDetailed(List.of())).isEmpty();
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private GlucoseReading reading(double value) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .value(BigDecimal.valueOf(value))
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}