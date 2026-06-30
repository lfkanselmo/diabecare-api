package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.SystemConfigPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertConfigAdapter")
class AlertConfigAdapterTest {

    @Mock
    private SystemConfigPort systemConfig;

    @InjectMocks
    private AlertConfigAdapter adapter;

    @Test
    @DisplayName("hoursWithoutGlucoseAlert usa la clave correcta")
    void hoursWithoutGlucoseAlertUsesCorrectKey() {
        when(systemConfig.getInt("alert.hours_without_glucose")).thenReturn(6);

        assertThat(adapter.hoursWithoutGlucoseAlert()).isEqualTo(6);
        verify(systemConfig).getInt("alert.hours_without_glucose");
    }

    @Test
    @DisplayName("minReadingsForStats usa la clave correcta")
    void minReadingsForStatsUsesCorrectKey() {
        when(systemConfig.getInt("alert.min_readings_for_stats")).thenReturn(3);

        assertThat(adapter.minReadingsForStats()).isEqualTo(3);
        verify(systemConfig).getInt("alert.min_readings_for_stats");
    }

    @Test
    @DisplayName("goodTirThreshold usa la clave correcta")
    void goodTirThresholdUsesCorrectKey() {
        when(systemConfig.getDecimal("alert.good_tir_threshold")).thenReturn(70.0);

        assertThat(adapter.goodTirThreshold()).isEqualTo(70.0);
        verify(systemConfig).getDecimal("alert.good_tir_threshold");
    }

    @Test
    @DisplayName("streakDays usa la clave correcta")
    void streakDaysUsesCorrectKey() {
        when(systemConfig.getInt("alert.streak_days")).thenReturn(7);

        assertThat(adapter.streakDays()).isEqualTo(7);
        verify(systemConfig).getInt("alert.streak_days");
    }

    @Test
    @DisplayName("daysBeforeOpenCycleAlert usa la clave correcta")
    void daysBeforeOpenCycleAlertUsesCorrectKey() {
        when(systemConfig.getInt("alert.days_before_open_cycle_alert")).thenReturn(10);

        assertThat(adapter.daysBeforeOpenCycleAlert()).isEqualTo(10);
        verify(systemConfig).getInt("alert.days_before_open_cycle_alert");
    }
}