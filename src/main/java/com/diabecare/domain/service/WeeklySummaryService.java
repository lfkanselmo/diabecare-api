package com.diabecare.domain.service;

import com.diabecare.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeeklySummaryService {

    private final MedicalCalculatorService calculator;

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
        return "📊 Tu resumen semanal — DiabeCare";
    }

    public String buildPushMessage(WeeklySummaryData data) {
        return String.format(
                "Promedio: %.0f mg/dL · TIR: %.0f%% · HbA1c est: %.1f%% · Hipos: %d · Hipers: %d",
                data.averageGlucose(),
                data.timeInRangePercent(),
                data.estimatedHba1c(),
                data.hypoEpisodes(),
                data.hyperEpisodes()
        );
    }
}