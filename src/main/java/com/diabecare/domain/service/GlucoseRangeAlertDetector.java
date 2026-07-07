package com.diabecare.domain.service;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.model.Alert;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Detecta ausencia de registro de glucosa, valores fuera de rango y HbA1c estimada elevada.
 */
@RequiredArgsConstructor
public class GlucoseRangeAlertDetector implements AlertDetector {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final AlertConfigPort alertConfig;
    private final MedicalCalculatorService medicalCalculatorService;
    private final SystemConfigPort systemConfig;
    private final MessageResolverPort messages;

    @Override
    public List<Alert> detect(Patient patient, LocalDateTime now) {
        List<Alert> alerts = new ArrayList<>();

        LocalDateTime from = now.minusHours(alertConfig.hoursWithoutGlucoseAlert());
        List<GlucoseReading> recentReadings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(), from, now);

        if (recentReadings.isEmpty()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.NO_GLUCOSE_RECORDED)
                    .severity(Alert.Severity.WARNING)
                    .title(messages.resolve("alert.no-glucose.title"))
                    .message(messages.resolve("alert.no-glucose.message",
                            alertConfig.hoursWithoutGlucoseAlert()))
                    .build());
            return alerts;
        }

        GlucoseReading latest = recentReadings.get(recentReadings.size() - 1);
        double value = latest.getValueInMgDl().doubleValue();

        if (value < 70) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.GLUCOSE_OUT_OF_RANGE)
                    .severity(Alert.Severity.DANGER)
                    .title(messages.resolve("alert.hypo.title"))
                    .message(messages.resolve("alert.hypo.message", value))
                    .build());
        } else if (value > patient.getTargetGlucoseMax().doubleValue()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.GLUCOSE_OUT_OF_RANGE)
                    .severity(Alert.Severity.WARNING)
                    .title(messages.resolve("alert.high.title"))
                    .message(messages.resolve("alert.high.message",
                            value, patient.getTargetGlucoseMax()))
                    .build());
        }

        LocalDateTime weekAgo = now.minusDays(7);
        List<GlucoseReading> weekReadings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(), weekAgo, now);

        if (weekReadings.size() >= alertConfig.minReadingsForStats()) {
            BigDecimal avg = medicalCalculatorService.calculateAverage(weekReadings);
            BigDecimal hba1c = medicalCalculatorService.estimateHba1c(avg);
            double hba1cThreshold = systemConfig.getDecimal("alert.hba1c_threshold");

            if (hba1c.doubleValue() > hba1cThreshold) {
                alerts.add(Alert.builder()
                        .type(Alert.AlertType.HIGH_HBA1C_ESTIMATED)
                        .severity(Alert.Severity.WARNING)
                        .title(messages.resolve("alert.hba1c-high.title"))
                        .message(messages.resolve("alert.hba1c-high.message", hba1c.doubleValue()))
                        .build());
            }
        }

        return alerts;
    }
}
