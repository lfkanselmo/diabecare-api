package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetAlertsUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.MedicalCalculatorService;
import com.diabecare.infrastructure.config.DiabeCareProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAlertsUseCaseImpl implements GetAlertsUseCase {

    private final LoadPatientPort loadPatientPort;
    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final LoadMealEntryPort loadMealEntryPort;
    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final MedicalCalculatorService medicalCalculatorService;
    private final DiabeCareProperties properties;

    @Override
    public List<Alert> getAlerts(UUID patientId) {
        Patient patient = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));

        List<Alert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        alerts.addAll(checkGlucoseAlerts(patient, now));
        alerts.addAll(checkCalorieAlert(patient, now));
        alerts.addAll(checkPositiveStreak(patient, now));
        alerts.addAll(checkMenstrualCycleAlert(patient));

        return alerts;
    }

    private List<Alert> checkGlucoseAlerts(Patient patient, LocalDateTime now) {
        List<Alert> alerts = new ArrayList<>();
        int hoursWithout = properties.clinical().hoursWithoutGlucoseAlert();

        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(),
                        now.minusHours(hoursWithout), now);

        if (readings.isEmpty()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.NO_GLUCOSE_RECORDED)
                    .severity(Alert.Severity.INFO)
                    .title("Sin registros recientes")
                    .message("No has registrado tu glucosa en las últimas " +
                            hoursWithout + " horas.")
                    .build());
            return alerts;
        }

        GlucoseReading latest = readings.stream()
                .max((a, b) -> a.getMeasuredAt().compareTo(b.getMeasuredAt()))
                .orElseThrow();

        if (!patient.isGlucoseInRange(latest.getValueInMgDl())) {
            boolean isHigh = latest.getValueInMgDl()
                    .compareTo(patient.getTargetGlucoseMax()) > 0;
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.GLUCOSE_OUT_OF_RANGE)
                    .severity(isHigh ? Alert.Severity.WARNING : Alert.Severity.DANGER)
                    .title(isHigh ? "Glucosa elevada" : "Glucosa baja")
                    .message("Tu última lectura fue de " + latest.getValueInMgDl() +
                            " mg/dL, fuera de tu rango objetivo (" +
                            patient.getTargetGlucoseMin() + " - " +
                            patient.getTargetGlucoseMax() + " mg/dL).")
                    .build());
        }

        List<GlucoseReading> weekReadings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(),
                        now.minusDays(7), now);

        int minReadings = properties.clinical().minReadingsForStats();
        if (weekReadings.size() >= minReadings) {
            BigDecimal avg = medicalCalculatorService.calculateAverage(weekReadings);
            if (avg.compareTo(patient.getTargetGlucoseMax()) > 0) {
                alerts.add(Alert.builder()
                        .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                        .severity(Alert.Severity.WARNING)
                        .title("Promedio semanal elevado")
                        .message("Tu glucosa promedio de los últimos 7 días es " +
                                avg + " mg/dL, por encima de tu objetivo.")
                        .build());
            }
        }

        return alerts;
    }

    private List<Alert> checkCalorieAlert(Patient patient, LocalDateTime now) {
        List<Alert> alerts = new ArrayList<>();
        if (patient.getDailyCalorieGoal() == null) return alerts;

        List<MealEntry> todayMeals = loadMealEntryPort
                .findByPatientIdAndDate(patient.getPatientId(), LocalDate.now());

        double totalCalories = todayMeals.stream()
                .mapToDouble(m -> m.getTotalCalories().doubleValue())
                .sum();

        if (totalCalories > patient.getDailyCalorieGoal()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.CALORIE_GOAL_EXCEEDED)
                    .severity(Alert.Severity.WARNING)
                    .title("Meta calórica superada")
                    .message(String.format("Has consumido %.0f kcal hoy, superando " +
                                    "tu meta de %d kcal.",
                            totalCalories, patient.getDailyCalorieGoal()))
                    .build());
        }

        return alerts;
    }

    private List<Alert> checkPositiveStreak(Patient patient, LocalDateTime now) {
        List<Alert> alerts = new ArrayList<>();
        int streakDays = properties.clinical().streakDays();
        double tirThreshold = properties.clinical().goodTirThreshold();

        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(),
                        now.minusDays(streakDays), now);

        int minReadings = properties.clinical().minReadingsForStats();
        if (readings.size() < minReadings) return alerts;

        BigDecimal tir = medicalCalculatorService.calculateTimeInRange(
                readings, patient.getTargetGlucoseMin(), patient.getTargetGlucoseMax());

        if (tir.doubleValue() >= tirThreshold) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.POSITIVE_STREAK)
                    .severity(Alert.Severity.SUCCESS)
                    .title("¡Excelente control!")
                    .message(String.format("Tu tiempo en rango los últimos %d días es %.1f%%. " +
                            "¡Sigue así!", streakDays, tir.doubleValue()))
                    .build());
        }

        return alerts;
    }

    private List<Alert> checkMenstrualCycleAlert(Patient patient) {
        List<Alert> alerts = new ArrayList<>();
        if (!patient.isFemale()) return alerts;

        loadMenstrualCyclePort.findLatestByPatientId(patient.getPatientId())
                .ifPresent(cycle -> {
                    CyclePhase phase = cycle.calculateCurrentPhase(LocalDate.now());
                    long daysUntilNext = java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDate.now(), cycle.predictNextCycleStart());

                    // Alerta por fase con impacto en glucosa
                    switch (phase) {
                        case LUTEAL_LATE -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title("Fase lútea tardía — Mayor resistencia a insulina")
                                .message(cycle.getPhaseGlucoseGuidance())
                                .build());
                        case LUTEAL_EARLY -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title("Fase lútea — Monitoreo frecuente recomendado")
                                .message(cycle.getPhaseGlucoseGuidance())
                                .build());
                        case OVULATION -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title("Período de ovulación")
                                .message(cycle.getPhaseGlucoseGuidance())
                                .build());
                        default -> {}
                    }

                    // Predicción de próximo ciclo
                    if (daysUntilNext == 3) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title("Tu período llega en 3 días")
                                .message("Prepárate para posibles cambios en tu glucosa. " +
                                        "La caída de hormonas puede causar variaciones importantes.")
                                .build());
                    } else if (daysUntilNext == 1) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title("Tu período llega mañana")
                                .message("Monitorea tu glucosa con mayor frecuencia hoy y mañana.")
                                .build());
                    } else if (daysUntilNext == 0) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title("Tu período comienza hoy")
                                .message("No olvides registrar el inicio de tu nuevo ciclo para mantener el seguimiento.")
                                .build());
                    }
                });

        return alerts;
    }
}