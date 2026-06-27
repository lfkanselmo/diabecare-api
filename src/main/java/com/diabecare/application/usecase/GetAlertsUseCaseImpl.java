package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetAlertsUseCase;
import com.diabecare.application.port.out.*;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.CycleStatisticsService;
import com.diabecare.domain.service.MedicalCalculatorService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.diabecare.domain.service.PatternDetectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAlertsUseCaseImpl implements GetAlertsUseCase {

    private final LoadPatientPort          loadPatientPort;
    private final LoadGlucoseReadingPort   loadGlucoseReadingPort;
    private final LoadMealEntryPort        loadMealEntryPort;
    private final LoadMenstrualCyclePort   loadMenstrualCyclePort;
    private final MedicalCalculatorService medicalCalculatorService;
    private final AlertConfigPort          alertConfig;
    private final PatternDetectorService   patternDetectorService;
    private final SystemConfigPort         systemConfig;
    private final MessageResolverPort      messages;
    private final MenstrualCycleGuidanceService cycleGuidanceService;
    private final CycleStatisticsService   cycleStatisticsService;

    @Override
    public List<Alert> getAlerts(UUID patientId) {
        Patient patient = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));

        LocalDateTime now = LocalDateTime.now();
        List<Alert> alerts = new ArrayList<>();

        alerts.addAll(checkGlucoseAlerts(patient, now));
        alerts.addAll(checkNutritionAlerts(patient, now));
        alerts.addAll(checkPositiveStreak(patient, now));
        alerts.addAll(checkPatternAlerts(patient, now));

        if (patient.isFemale()) {
            alerts.addAll(checkMenstrualCycleAlert(patient));
        }

        return alerts;
    }

    private List<Alert> checkGlucoseAlerts(Patient patient, LocalDateTime now) {
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
            BigDecimal avg   = medicalCalculatorService.calculateAverage(weekReadings);
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

    private List<Alert> checkNutritionAlerts(Patient patient, LocalDateTime now) {
        List<Alert> alerts = new ArrayList<>();

        List<MealEntry> todayMeals = loadMealEntryPort
                .findByPatientIdAndDate(patient.getPatientId(), now.toLocalDate());

        if (todayMeals.isEmpty()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.NO_MEAL_RECORDED)
                    .severity(Alert.Severity.INFO)
                    .title(messages.resolve("alert.no-meal.title"))
                    .message(messages.resolve("alert.no-meal.message"))
                    .build());
        }

        return alerts;
    }

    private List<Alert> checkPositiveStreak(Patient patient, LocalDateTime now) {
        List<Alert> alerts = new ArrayList<>();

        LocalDateTime from = now.minusDays(alertConfig.streakDays());
        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(), from, now);

        if (readings.size() < alertConfig.minReadingsForStats()) return alerts;

        BigDecimal tir = medicalCalculatorService.calculateTimeInRange(
                readings, patient.getTargetGlucoseMin(), patient.getTargetGlucoseMax());

        if (tir.doubleValue() >= alertConfig.goodTirThreshold()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.POSITIVE_STREAK)
                    .severity(Alert.Severity.SUCCESS)
                    .title(messages.resolve("alert.positive-streak.title"))
                    .message(messages.resolve("alert.positive-streak.message",
                            alertConfig.streakDays(), tir.doubleValue()))
                    .build());
        }

        return alerts;
    }

    private List<Alert> checkMenstrualCycleAlert(Patient patient) {
        List<Alert> alerts = new ArrayList<>();

        loadMenstrualCyclePort.findLatestByPatientId(patient.getPatientId())
                .ifPresent(cycle -> {
                    List<MenstrualCycle> history = loadMenstrualCyclePort
                            .findByPatientId(patient.getPatientId());
                    Integer avgCycleLength = cycleStatisticsService.calculateAverageCycleLength(history);
                    Integer avgPeriodLength = cycleStatisticsService.calculateAveragePeriodLength(history);

                    CyclePhase phase = cycle.calculateCurrentPhase(
                            java.time.LocalDate.now(), avgCycleLength, avgPeriodLength);
                    long daysUntilNext = java.time.temporal.ChronoUnit.DAYS.between(
                            java.time.LocalDate.now(), cycle.predictNextCycleStart(avgCycleLength));

                    switch (phase) {
                        case LUTEAL_LATE -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title(messages.resolve("alert.cycle.luteal-late.title"))
                                .message(cycleGuidanceService.resolveGuidance(phase))
                                .build());
                        case LUTEAL_EARLY -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title(messages.resolve("alert.cycle.luteal-early.title"))
                                .message(cycleGuidanceService.resolveGuidance(phase))
                                .build());
                        case OVULATION -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title(messages.resolve("alert.cycle.ovulation.title"))
                                .message(cycleGuidanceService.resolveGuidance(phase))
                                .build());
                        default -> {}
                    }

                    if (daysUntilNext == 3) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title(messages.resolve("alert.cycle.period-3days.title"))
                                .message(messages.resolve("alert.cycle.period-3days.message"))
                                .build());
                    } else if (daysUntilNext == 1) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title(messages.resolve("alert.cycle.period-tomorrow.title"))
                                .message(messages.resolve("alert.cycle.period-tomorrow.message"))
                                .build());
                    } else if (daysUntilNext == 0) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title(messages.resolve("alert.cycle.period-today.title"))
                                .message(messages.resolve("alert.cycle.period-today.message"))
                                .build());
                    }
                });

        return alerts;
    }

    private List<Alert> checkPatternAlerts(Patient patient, LocalDateTime now) {
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
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
    }
}