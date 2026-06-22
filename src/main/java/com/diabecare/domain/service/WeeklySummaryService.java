package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.*;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WeeklySummaryService {

    private final MedicalCalculatorService calculator;
    private final MessageResolverPort      messages;

    public Optional<WeeklySummaryData> buildSummary(Patient patient, List<GlucoseReading> readings) {
        if (readings.isEmpty()) return Optional.empty();

        BigDecimal avg   = calculator.calculateAverage(readings);
        BigDecimal hba1c = calculator.estimateHba1c(avg);
        BigDecimal tir   = calculator.calculateTimeInRange(
                readings, patient.getTargetGlucoseMin(), patient.getTargetGlucoseMax());

        long hypo  = readings.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() < 70)
                .count();
        long hyper = readings.stream()
                .filter(r -> r.getValueInMgDl().doubleValue() > patient.getTargetGlucoseMax().doubleValue())
                .count();

        return Optional.of(new WeeklySummaryData(
                patient.getPatientId(),
                patient.getFullName(),
                avg, hba1c, tir,
                hypo, hyper,
                readings.size()
        ));
    }

    public String buildPushTitle() {
        return messages.resolve("weekly-summary.push.title");
    }

    public String buildPushMessage(WeeklySummaryData data) {
        return messages.resolve("weekly-summary.push.message",
                data.averageGlucose(),
                data.timeInRangePercent(),
                data.estimatedHba1c(),
                data.hypoEpisodes(),
                data.hyperEpisodes()
        );
    }
}