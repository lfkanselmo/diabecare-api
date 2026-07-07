package com.diabecare.domain.service;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.Alert;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detecta una racha positiva de buen tiempo en rango durante los últimos N días.
 */
@RequiredArgsConstructor
public class PositiveStreakAlertDetector implements AlertDetector {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final AlertConfigPort alertConfig;
    private final MedicalCalculatorService medicalCalculatorService;
    private final MessageResolverPort messages;

    @Override
    public List<Alert> detect(Patient patient, LocalDateTime now) {
        LocalDateTime from = now.minusDays(alertConfig.streakDays());
        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(), from, now);

        if (readings.size() < alertConfig.minReadingsForStats()) return List.of();

        BigDecimal tir = medicalCalculatorService.calculateTimeInRange(
                readings, patient.getTargetGlucoseMin(), patient.getTargetGlucoseMax());

        if (tir.doubleValue() >= alertConfig.goodTirThreshold()) {
            return List.of(Alert.builder()
                    .type(Alert.AlertType.POSITIVE_STREAK)
                    .severity(Alert.Severity.SUCCESS)
                    .title(messages.resolve("alert.positive-streak.title"))
                    .message(messages.resolve("alert.positive-streak.message",
                            alertConfig.streakDays(), tir.doubleValue()))
                    .build());
        }

        return List.of();
    }
}
