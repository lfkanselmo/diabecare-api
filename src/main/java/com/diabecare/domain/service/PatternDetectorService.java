package com.diabecare.domain.service;

import com.diabecare.application.port.out.SystemConfigPort;
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
    private final SystemConfigPort         systemConfig;

    public Optional<Alert> detectHighFastingPattern(List<GlucoseReading> readings) {
        int    threshold = systemConfig.getInt("pattern.fasting_threshold_mgdl");
        double ratio     = systemConfig.getDecimal("pattern.fasting_ratio");
        int    minCount  = systemConfig.getInt("alert.min_readings_for_stats");

        List<GlucoseReading> fasting = readings.stream()
                .filter(r -> r.getReadingType() == ReadingType.FASTING)
                .toList();

        if (fasting.size() < minCount) return Optional.empty();

        long highCount = fasting.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() > threshold)
                .count();

        if (highCount < fasting.size() * ratio) return Optional.empty();

        return Optional.of(Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.WARNING)
                .title("Patrón: glucosa alta en ayuno")
                .message(String.format(
                        "%d de tus últimas %d lecturas de ayuno superaron %d mg/dL. " +
                                "Considera ajustar tu insulina basal o consultar a tu médico.",
                        highCount, fasting.size(), threshold))
                .build());
    }

    public Optional<Alert> detectHighPostMealPattern(List<GlucoseReading> readings) {
        int    threshold = systemConfig.getInt("pattern.postmeal_threshold_mgdl");
        double ratio     = systemConfig.getDecimal("pattern.postmeal_ratio");
        int    minCount  = systemConfig.getInt("alert.min_readings_for_stats");

        List<GlucoseReading> postMeal = readings.stream()
                .filter(r -> r.getReadingType() == ReadingType.POST_MEAL)
                .toList();

        if (postMeal.size() < minCount) return Optional.empty();

        long highCount = postMeal.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() > threshold)
                .count();

        if (highCount < postMeal.size() * ratio) return Optional.empty();

        return Optional.of(Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.WARNING)
                .title("Patrón: picos postprandiales frecuentes")
                .message(String.format(
                        "%d de tus últimas %d lecturas postprandiales superaron %d mg/dL. " +
                                "Revisa el tamaño de tus porciones o ajusta la insulina bolo.",
                        highCount, postMeal.size(), threshold))
                .build());
    }

    public Optional<Alert> detectRecurrentHypoglycemia(List<GlucoseReading> readings) {
        int minEpisodes = systemConfig.getInt("pattern.hypo_min_episodes");
        int daysWindow  = systemConfig.getInt("pattern.days_window");

        long hypoCount = readings.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() < 70)
                .count();

        if (hypoCount < minEpisodes) return Optional.empty();

        return Optional.of(Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.DANGER)
                .title("Patrón: hipoglucemias frecuentes")
                .message(String.format(
                        "Has tenido %d episodios de hipoglucemia en los últimos %d días. " +
                                "Consulta a tu médico para revisar tu esquema de insulina.",
                        hypoCount, daysWindow))
                .build());
    }

    public Optional<Alert> detectHighVariability(List<GlucoseReading> readings) {
        int    minReadings = systemConfig.getInt("pattern.min_readings_variability");
        double cvThreshold = systemConfig.getDecimal("pattern.cv_threshold");

        if (readings.size() < minReadings) return Optional.empty();

        BigDecimal avg = calculator.calculateAverage(readings);
        BigDecimal std = calculator.calculateStandardDeviation(readings);
        BigDecimal cv  = calculator.calculateCoefficientOfVariation(std, avg);

        if (cv.doubleValue() < cvThreshold) return Optional.empty();

        return Optional.of(Alert.builder()
                .type(Alert.AlertType.GLUCOSE_PATTERN_DETECTED)
                .severity(Alert.Severity.WARNING)
                .title("Patrón: alta variabilidad glucémica")
                .message(String.format(
                        "Tu coeficiente de variación es %.0f%% (objetivo: <%.0f%%). " +
                                "Una alta variabilidad aumenta el riesgo de complicaciones.",
                        cv.doubleValue(), cvThreshold))
                .build());
    }
}