package com.diabecare.application.port.out;

public interface AlertConfigPort {
    int hoursWithoutGlucoseAlert();
    int minReadingsForStats();
    double goodTirThreshold();
    int streakDays();
}