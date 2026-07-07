package com.diabecare.domain.service;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.model.Alert;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Envuelve {@link PatternDetectorService} para exponerlo como un {@link AlertDetector} más.
 */
@RequiredArgsConstructor
public class ClinicalPatternAlertDetector implements AlertDetector {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final AlertConfigPort alertConfig;
    private final SystemConfigPort systemConfig;
    private final PatternDetectorService patternDetectorService;

    @Override
    public List<Alert> detect(Patient patient, LocalDateTime now) {
        int daysWindow = systemConfig.getInt("pattern.days_window");

        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(), now.minusDays(daysWindow), now);

        if (readings.size() < alertConfig.minReadingsForStats()) return List.of();

        return List.of(
                        patternDetectorService.detectHighFastingPattern(readings),
                        patternDetectorService.detectHighPostMealPattern(readings),
                        patternDetectorService.detectRecurrentHypoglycemia(readings),
                        patternDetectorService.detectHighVariability(readings)
                ).stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
