package com.diabecare.domain.service;

import com.diabecare.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PatternDetectorService {

    private final MedicalCalculatorService calculator;

    public Optional<Alert> detectHighFastingPattern(List<GlucoseReading> readings) {
        List<GlucoseReading> fasting = readings.stream()
                .filter(r -> r.getReadingType() == ReadingType.FASTING)
                .toList();

        if (fasting.size() < 3) return Optional.empty();

        long highCount = fasting.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() > 130)
                .count();

        if (highCount < fasting.size() * 0.6) return Optional.empty();

        return Optional.of(Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.WARNING)
                .title("Patrón: glucosa alta en ayuno")
                .message(String.format(
                        "%d de tus últimas %d lecturas de ayuno superaron 130 mg/dL. " +
                                "Considera ajustar tu insulina basal o consultar a tu médico.",
                        highCount, fasting.size()))
                .build());
    }

    public Optional<Alert> detectHighPostMealPattern(List<GlucoseReading> readings) {
        List<GlucoseReading> postMeal = readings.stream()
                .filter(r -> r.getReadingType() == ReadingType.POST_MEAL)
                .toList();

        if (postMeal.size() < 3) return Optional.empty();

        long highCount = postMeal.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() > 180)
                .count();

        if (highCount < postMeal.size() * 0.5) return Optional.empty();

        return Optional.of(Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.WARNING)
                .title("Patrón: picos postprandiales frecuentes")
                .message(String.format(
                        "%d de tus últimas %d lecturas postprandiales superaron 180 mg/dL. " +
                                "Revisa el tamaño de tus porciones o ajusta la insulina bolo.",
                        highCount, postMeal.size()))
                .build());
    }

    public Optional<Alert> detectRecurrentHypoglycemia(List<GlucoseReading> readings) {
        long hypoCount = readings.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() < 70)
                .count();

        if (hypoCount < 3) return Optional.empty();

        return Optional.of(Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.DANGER)
                .title("Patrón: hipoglucemias frecuentes")
                .message(String.format(
                        "Has tenido %d episodios de hipoglucemia en los últimos 14 días. " +
                                "Consulta a tu médico para revisar tu esquema de insulina.",
                        hypoCount))
                .build());
    }

    public Optional<Alert> detectHighVariability(List<GlucoseReading> readings) {
        if (readings.size() < 7) return Optional.empty();

        BigDecimal avg = calculator.calculateAverage(readings);
        BigDecimal std = calculator.calculateStandardDeviation(readings);
        BigDecimal cv  = calculator.calculateCoefficientOfVariation(std, avg);

        if (cv.doubleValue() < 36) return Optional.empty();

        return Optional.of(Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.WARNING)
                .title("Patrón: alta variabilidad glucémica")
                .message(String.format(
                        "Tu coeficiente de variación es %.0f%% (objetivo: <36%%). " +
                                "Una alta variabilidad aumenta el riesgo de complicaciones.",
                        cv.doubleValue()))
                .build());
    }
}