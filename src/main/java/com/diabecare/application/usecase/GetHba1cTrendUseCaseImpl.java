package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetHba1cTrendUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.service.MedicalCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetHba1cTrendUseCaseImpl implements GetHba1cTrendUseCase {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final MedicalCalculatorService medicalCalculatorService;

    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    public List<MonthlyHba1c> getTrend(UUID patientId, int months) {
        List<MonthlyHba1c> trend = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            LocalDateTime from = month.atDay(1).atStartOfDay();
            LocalDateTime to   = month.atEndOfMonth().atTime(23, 59, 59);

            List<GlucoseReading> readings = loadGlucoseReadingPort
                    .findByPatientIdAndDateRange(patientId, from, to);

            if (readings.isEmpty()) {
                trend.add(new MonthlyHba1c(
                        month.format(MONTH_FMT), null, null, 0));
                continue;
            }

            BigDecimal avg    = medicalCalculatorService.calculateAverage(readings);
            BigDecimal hba1c  = medicalCalculatorService.estimateHba1c(avg);

            trend.add(new MonthlyHba1c(
                    month.format(MONTH_FMT), hba1c, avg, readings.size()));
        }

        return trend;
    }
}