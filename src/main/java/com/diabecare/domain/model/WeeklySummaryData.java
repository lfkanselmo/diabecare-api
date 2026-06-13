package com.diabecare.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record WeeklySummaryData(
        UUID       patientId,
        String     patientName,
        BigDecimal averageGlucose,
        BigDecimal estimatedHba1c,
        BigDecimal timeInRangePercent,
        long       hypoEpisodes,
        long       hyperEpisodes,
        int        totalReadings
) {}