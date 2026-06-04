package com.diabecare.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GetHba1cTrendUseCase {

    record MonthlyHba1c(
            String month,
            BigDecimal estimatedHba1c,
            BigDecimal averageGlucose,
            int totalReadings
    ) {}

    List<MonthlyHba1c> getTrend(UUID patientId, int months);
}