package com.diabecare.domain.service;

import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseStatus;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MedicalCalculatorService {

    private static final BigDecimal HBAC1_DIVISOR = BigDecimal.valueOf(28.7);
    private static final BigDecimal HBAC1_OFFSET = BigDecimal.valueOf(46.7);

    public BigDecimal estimateHba1c(BigDecimal averageGlucoseMgDl) {
        return averageGlucoseMgDl.add(HBAC1_OFFSET)
                .divide(HBAC1_DIVISOR, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateAverage(List<GlucoseReading> readings) {
        if (readings.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = readings.stream()
                .map(GlucoseReading::getValueInMgDl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(readings.size()), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateStandardDeviation(List<GlucoseReading> readings) {
        if (readings.size() < 2) return BigDecimal.ZERO;
        BigDecimal mean = calculateAverage(readings);
        BigDecimal variance = readings.stream()
                .map(r -> r.getValueInMgDl().subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(readings.size()), 4, RoundingMode.HALF_UP);
        return variance.sqrt(new MathContext(6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateCoefficientOfVariation(BigDecimal standardDeviation,
                                                      BigDecimal mean) {
        if (mean.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return standardDeviation
                .divide(mean, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTimeInRange(List<GlucoseReading> readings,
                                           BigDecimal targetMin,
                                           BigDecimal targetMax) {
        if (readings.isEmpty()) return BigDecimal.ZERO;
        long inRange = readings.stream()
                .filter(r -> {
                    BigDecimal v = r.getValueInMgDl();
                    return v.compareTo(targetMin) >= 0 && v.compareTo(targetMax) <= 0;
                })
                .count();
        return BigDecimal.valueOf(inRange)
                .divide(BigDecimal.valueOf(readings.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTimeByStatus(List<GlucoseReading> readings,
                                            GlucoseStatus status) {
        if (readings.isEmpty()) return BigDecimal.ZERO;
        long count = readings.stream()
                .filter(r -> r.getStatus() == status)
                .count();
        return BigDecimal.valueOf(count)
                .divide(BigDecimal.valueOf(readings.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public int calculateDailyCalorieNeeds(BigDecimal weightKg,
                                          BigDecimal heightCm,
                                          int age,
                                          boolean isMale,
                                          ActivityLevel activityLevel) {
        double tmb = isMale
                ? (10 * weightKg.doubleValue()) + (6.25 * heightCm.doubleValue()) - (5 * age) + 5
                : (10 * weightKg.doubleValue()) + (6.25 * heightCm.doubleValue()) - (5 * age) - 161;

        double factor = switch (activityLevel) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
        };

        return (int) Math.round(tmb * factor);
    }

    public Map<String, BigDecimal> calculateTirDetailed(List<GlucoseReading> readings) {
        if (readings.isEmpty()) return Map.of();

        long total = readings.size();
        long veryLow  = readings.stream().filter(r -> r.getValueInMgDl().doubleValue() < 54).count();
        long low      = readings.stream().filter(r -> {
            double v = r.getValueInMgDl().doubleValue();
            return v >= 54 && v < 70;
        }).count();
        long inRange  = readings.stream().filter(r -> {
            double v = r.getValueInMgDl().doubleValue();
            return v >= 70 && v <= 180;
        }).count();
        long high     = readings.stream().filter(r -> {
            double v = r.getValueInMgDl().doubleValue();
            return v > 180 && v <= 250;
        }).count();
        long veryHigh = readings.stream().filter(r -> r.getValueInMgDl().doubleValue() > 250).count();

        return Map.of(
                "veryLow",  round((veryLow  * 100.0) / total),
                "low",      round((low      * 100.0) / total),
                "inRange",  round((inRange  * 100.0) / total),
                "high",     round((high     * 100.0) / total),
                "veryHigh", round((veryHigh * 100.0) / total)
        );
    }

    public Map<String, BigDecimal> calculateAverageByReadingType(List<GlucoseReading> readings) {
        if (readings.isEmpty()) return Map.of();

        Map<String, BigDecimal> result = new java.util.LinkedHashMap<>();
        for (var type : com.diabecare.domain.model.ReadingType.values()) {
            readings.stream()
                    .filter(r -> r.getReadingType() == type)
                    .map(GlucoseReading::getValueInMgDl)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var filtered = readings.stream()
                    .filter(r -> r.getReadingType() == type)
                    .toList();

            if (!filtered.isEmpty()) {
                result.put(type.name(), calculateAverage(filtered));
            }
        }
        return result;
    }

    public List<GlucoseReading> getHypoglycemiaEvents(List<GlucoseReading> readings) {
        return readings.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() < 70)
                .sorted((a, b) -> a.getMeasuredAt().compareTo(b.getMeasuredAt()))
                .toList();
    }

    public double calculateAdherencePercent(List<GlucoseReading> readings,
                                            LocalDateTime from, LocalDateTime to) {
        if (readings.isEmpty()) return 0.0;
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate()) + 1;
        long daysWithReadings = readings.stream()
                .map(r -> r.getMeasuredAt().toLocalDate())
                .distinct()
                .count();
        return Math.min(100.0, (daysWithReadings * 100.0) / totalDays);
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(Math.round(value * 10.0) / 10.0);
    }
}