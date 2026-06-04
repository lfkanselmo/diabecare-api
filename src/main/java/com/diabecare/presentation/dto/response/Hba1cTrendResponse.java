package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;

public record Hba1cTrendResponse(
        String month,
        BigDecimal estimatedHba1c,
        BigDecimal averageGlucose,
        int totalReadings
) {}