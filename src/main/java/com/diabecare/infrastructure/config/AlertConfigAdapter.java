package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.SystemConfigPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertConfigAdapter implements AlertConfigPort {

    private final SystemConfigPort systemConfig;

    @Override
    public int hoursWithoutGlucoseAlert() {
        return systemConfig.getInt("alert.hours_without_glucose");
    }

    @Override
    public int minReadingsForStats() {
        return systemConfig.getInt("alert.min_readings_for_stats");
    }

    @Override
    public double goodTirThreshold() {
        return systemConfig.getDecimal("alert.good_tir_threshold");
    }

    @Override
    public int streakDays() {
        return systemConfig.getInt("alert.streak_days");
    }
}