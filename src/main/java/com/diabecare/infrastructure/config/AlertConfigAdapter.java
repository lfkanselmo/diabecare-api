package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.AlertConfigPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertConfigAdapter implements AlertConfigPort {

    private final DiabeCareProperties properties;

    @Override
    public int hoursWithoutGlucoseAlert() {
        return properties.clinical().hoursWithoutGlucoseAlert();
    }

    @Override
    public int minReadingsForStats() {
        return properties.clinical().minReadingsForStats();
    }

    @Override
    public double goodTirThreshold() {
        return properties.clinical().goodTirThreshold();
    }

    @Override
    public int streakDays() {
        return properties.clinical().streakDays();
    }
}