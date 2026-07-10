package com.diabecare.domain.service;

import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseStatus;
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

    @Nested
    @DisplayName("calculateStandardDeviation")
    class CalculateStandardDeviation {

        @Test
        @DisplayName("retorna cero con menos de 2 lecturas")
        void returnsZeroWithFewerThanTwoReadings() {
            assertThat(calculator.calculateStandardDeviation(List.of())).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(calculator.calculateStandardDeviation(List.of(reading(100))))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("calcula la desviación estándar poblacional correctamente")
        void calculatesCorrectly() {
            List<GlucoseReading> readings = List.of(
                    reading(100), reading(120), reading(80), reading(110), reading(90));

            BigDecimal std = calculator.calculateStandardDeviation(readings);

            assertThat(std.doubleValue()).isCloseTo(14.14, within(0.01));
        }

        @Test
        @DisplayName("retorna cero cuando todos los valores son idénticos")
        void returnsZeroWhenAllValuesIdentical() {
            List<GlucoseReading> readings = List.of(reading(100), reading(100), reading(100));
            assertThat(calculator.calculateStandardDeviation(readings)).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("calculateTimeByStatus")
    class CalculateTimeByStatus {

        @Test
        @DisplayName("calcula el porcentaje correcto para el estado solicitado")
        void calculatesCorrectPercentage() {
            List<GlucoseReading> readings = List.of(
                    reading(100), // NORMAL
                    reading(110), // NORMAL
                    reading(50),  // CRITICALLY_LOW
                    reading(300)  // CRITICALLY_HIGH
            );

            BigDecimal percent = calculator.calculateTimeByStatus(readings, GlucoseStatus.NORMAL);

            assertThat(percent).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("retorna cero cuando ninguna lectura coincide con el estado")
        void returnsZeroWhenNoMatch() {
            List<GlucoseReading> readings = List.of(reading(100), reading(110));
            BigDecimal percent = calculator.calculateTimeByStatus(readings, GlucoseStatus.CRITICALLY_LOW);
            assertThat(percent).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("retorna cero con lista vacía")
        void returnsZeroWhenEmpty() {
            assertThat(calculator.calculateTimeByStatus(List.of(), GlucoseStatus.NORMAL))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("calculateDailyCalorieNeeds")
    class CalculateDailyCalorieNeeds {

        @Test
        @DisplayName("calcula correctamente para un hombre sedentario")
        void calculatesCorrectlyForSedentaryMale() {
            int result = calculator.calculateDailyCalorieNeeds(
                    BigDecimal.valueOf(70), BigDecimal.valueOf(175), 30, true, ActivityLevel.SEDENTARY);

            // TMB = 1648.75, x1.2 = 1978.5 -> Math.round en Java redondea siempre hacia arriba en .5, da 1979
            assertThat(result).isEqualTo(1979);
        }

        @Test
        @DisplayName("calcula correctamente para una mujer moderadamente activa")
        void calculatesCorrectlyForModeratelyActiveFemale() {
            int result = calculator.calculateDailyCalorieNeeds(
                    BigDecimal.valueOf(70), BigDecimal.valueOf(175), 30, false, ActivityLevel.MODERATELY_ACTIVE);

            assertThat(result).isEqualTo(2298);
        }

        @Test
        @DisplayName("usa una fórmula distinta para hombres y mujeres con los mismos datos")
        void usesDifferentFormulaForMaleAndFemale() {
            int male = calculator.calculateDailyCalorieNeeds(
                    BigDecimal.valueOf(70), BigDecimal.valueOf(175), 30, true, ActivityLevel.SEDENTARY);
            int female = calculator.calculateDailyCalorieNeeds(
                    BigDecimal.valueOf(70), BigDecimal.valueOf(175), 30, false, ActivityLevel.SEDENTARY);

            assertThat(male).isGreaterThan(female);
        }

        @Test
        @DisplayName("a mayor nivel de actividad, mayor necesidad calórica")
        void higherActivityMeansHigherNeeds() {
            int sedentary = calculator.calculateDailyCalorieNeeds(
                    BigDecimal.valueOf(70), BigDecimal.valueOf(175), 30, true, ActivityLevel.SEDENTARY);
            int veryActive = calculator.calculateDailyCalorieNeeds(
                    BigDecimal.valueOf(70), BigDecimal.valueOf(175), 30, true, ActivityLevel.VERY_ACTIVE);

            assertThat(veryActive).isGreaterThan(sedentary);
        }
    }

    @Nested
    @DisplayName("calculateAverageByReadingType")
    class CalculateAverageByReadingType {

        @Test
        @DisplayName("calcula el promedio correcto agrupando por tipo de lectura")
        void calculatesAverageGroupedByType() {
            List<GlucoseReading> readings = List.of(
                    readingOfType(100, ReadingType.FASTING),
                    readingOfType(120, ReadingType.FASTING),
                    readingOfType(200, ReadingType.POST_MEAL)
            );

            var result = calculator.calculateAverageByReadingType(readings);

            assertThat(result.get("FASTING")).isEqualByComparingTo(new BigDecimal("110.00"));
            assertThat(result.get("POST_MEAL")).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("no incluye tipos de lectura sin ninguna lectura registrada")
        void excludesTypesWithoutReadings() {
            List<GlucoseReading> readings = List.of(readingOfType(100, ReadingType.FASTING));

            var result = calculator.calculateAverageByReadingType(readings);

            assertThat(result).containsOnlyKeys("FASTING");
        }

        @Test
        @DisplayName("retorna mapa vacío con lista vacía")
        void returnsEmptyWhenEmpty() {
            assertThat(calculator.calculateAverageByReadingType(List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("getHypoglycemiaEvents")
    class GetHypoglycemiaEvents {

        @Test
        @DisplayName("filtra solo las lecturas por debajo de 70 mg/dL")
        void filtersOnlyValuesBelow70() {
            List<GlucoseReading> readings = List.of(
                    reading(65), reading(100), reading(60), reading(150));

            List<GlucoseReading> hypoEvents = calculator.getHypoglycemiaEvents(readings);

            assertThat(hypoEvents).hasSize(2);
            assertThat(hypoEvents).allMatch(r -> r.getValueInMgDl().doubleValue() < 70);
        }

        @Test
        @DisplayName("ordena los eventos por fecha de medición ascendente")
        void ordersEventsByMeasuredAtAscending() {
            LocalDateTime now = LocalDateTime.now();
            GlucoseReading later = readingAt(60, now);
            GlucoseReading earlier = readingAt(65, now.minusHours(2));

            List<GlucoseReading> hypoEvents = calculator.getHypoglycemiaEvents(List.of(later, earlier));

            assertThat(hypoEvents).containsExactly(earlier, later);
        }

        @Test
        @DisplayName("retorna lista vacía cuando ninguna lectura es hipoglucemia")
        void returnsEmptyWhenNoHypoglycemia() {
            List<GlucoseReading> readings = List.of(reading(100), reading(150));
            assertThat(calculator.getHypoglycemiaEvents(readings)).isEmpty();
        }
    }

    @Nested
    @DisplayName("calculateAdherencePercent")
    class CalculateAdherencePercent {

        @Test
        @DisplayName("retorna 0 con lista vacía")
        void returnsZeroWhenEmpty() {
            LocalDateTime from = LocalDateTime.now().minusDays(10);
            LocalDateTime to = LocalDateTime.now();
            assertThat(calculator.calculateAdherencePercent(List.of(), from, to)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("calcula correctamente el porcentaje de días con al menos un registro")
        void calculatesCorrectPercentage() {
            LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
            LocalDateTime to = LocalDateTime.of(2026, 6, 10, 23, 59);

            // 5 días distintos con registros, de un total de 10 días en el rango
            List<GlucoseReading> readings = List.of(
                    readingAt(100, LocalDateTime.of(2026, 6, 1, 8, 0)),
                    readingAt(100, LocalDateTime.of(2026, 6, 2, 8, 0)),
                    readingAt(100, LocalDateTime.of(2026, 6, 3, 8, 0)),
                    readingAt(100, LocalDateTime.of(2026, 6, 4, 8, 0)),
                    readingAt(100, LocalDateTime.of(2026, 6, 5, 8, 0))
            );

            double adherence = calculator.calculateAdherencePercent(readings, from, to);

            assertThat(adherence).isEqualTo(50.0);
        }

        @Test
        @DisplayName("cuenta un mismo día solo una vez aunque haya múltiples lecturas")
        void countsSameDayOnlyOnce() {
            LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
            LocalDateTime to = LocalDateTime.of(2026, 6, 1, 23, 59);

            List<GlucoseReading> readings = List.of(
                    readingAt(100, LocalDateTime.of(2026, 6, 1, 8, 0)),
                    readingAt(110, LocalDateTime.of(2026, 6, 1, 14, 0)),
                    readingAt(120, LocalDateTime.of(2026, 6, 1, 20, 0))
            );

            double adherence = calculator.calculateAdherencePercent(readings, from, to);

            assertThat(adherence).isEqualTo(100.0);
        }

        @Test
        @DisplayName("nunca supera el 100% aunque haya más días con registro que días en el rango")
        void neverExceeds100Percent() {
            LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
            LocalDateTime to = LocalDateTime.of(2026, 6, 1, 23, 59);

            // Lecturas en días fuera del rango consultado, pero el cálculo de días únicos
            // no debería poder superar el 100% del rango total
            List<GlucoseReading> readings = List.of(
                    readingAt(100, LocalDateTime.of(2026, 6, 1, 8, 0)),
                    readingAt(100, LocalDateTime.of(2026, 5, 30, 8, 0)),
                    readingAt(100, LocalDateTime.of(2026, 5, 29, 8, 0))
            );

            double adherence = calculator.calculateAdherencePercent(readings, from, to);

            assertThat(adherence).isLessThanOrEqualTo(100.0);
        }
    }


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

    private GlucoseReading readingOfType(double value, ReadingType type) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .value(BigDecimal.valueOf(value))
                .unit(GlucoseUnit.MG_DL)
                .readingType(type)
                .measuredAt(LocalDateTime.now())
                .build();
    }

    private GlucoseReading readingAt(double value, LocalDateTime measuredAt) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .value(BigDecimal.valueOf(value))
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(measuredAt)
                .build();
    }
}
