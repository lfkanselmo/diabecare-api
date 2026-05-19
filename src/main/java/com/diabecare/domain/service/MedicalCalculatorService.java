package com.diabecare.domain.service;

import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseStatus;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

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
}