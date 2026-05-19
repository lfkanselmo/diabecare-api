package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;

public record GlucoseStatsResponse(
        BigDecimal average,
        BigDecimal standardDeviation,
        BigDecimal coefficientOfVariation,
        BigDecimal estimatedHba1c,
        BigDecimal timeInRangePercent,
        BigDecimal timeBelowRangePercent,
        BigDecimal timeAboveRangePercent,
        int totalReadings
) {}