package com.diabecare.application.dto;

import java.math.BigDecimal;

public record GlucoseStatsRecord(
        BigDecimal average,
        BigDecimal standardDeviation,
        BigDecimal coefficientOfVariation,
        BigDecimal estimatedHba1c,
        BigDecimal timeInRangePercent,
        BigDecimal timeBelowRangePercent,
        BigDecimal timeAboveRangePercent,
        int totalReadings
) {}