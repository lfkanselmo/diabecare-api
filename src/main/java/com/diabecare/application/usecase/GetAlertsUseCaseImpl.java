package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetAlertsUseCase;
import com.diabecare.application.port.out.*;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.MedicalCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAlertsUseCaseImpl implements GetAlertsUseCase {

    private final LoadPatientPort          loadPatientPort;
    private final LoadGlucoseReadingPort   loadGlucoseReadingPort;
    private final LoadMealEntryPort        loadMealEntryPort;
    private final LoadMenstrualCyclePort   loadMenstrualCyclePort;
    private final MedicalCalculatorService medicalCalculatorService;
    private final AlertConfigPort          alertConfig;

    @Override
    public List<Alert> getAlerts(UUID patientId) {
        Patient patient = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));

        LocalDateTime now = LocalDateTime.now();
        List<Alert> alerts = new ArrayList<>();

        alerts.addAll(checkGlucoseAlerts(patient, now));
        alerts.addAll(checkNutritionAlerts(patient, now));
        alerts.addAll(checkPositiveStreak(patient, now));

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
                    .title("Sin registro de glucosa")
                    .message("No has registrado tu glucosa en las últimas " +
                            alertConfig.hoursWithoutGlucoseAlert() + " horas.")
                    .build());
            return alerts;
        }

        GlucoseReading latest = recentReadings.get(recentReadings.size() - 1);
        double value = latest.getValueInMgDl().doubleValue();

        if (value < 70) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.GLUCOSE_OUT_OF_RANGE)
                    .severity(Alert.Severity.DANGER)
                    .title("⚠ Hipoglucemia detectada")
                    .message(String.format("Tu última lectura fue %.0f mg/dL. Consume carbohidratos de acción rápida.", value))
                    .build());
        } else if (value > patient.getTargetGlucoseMax().doubleValue()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.GLUCOSE_OUT_OF_RANGE)
                    .severity(Alert.Severity.WARNING)
                    .title("Glucosa elevada")
                    .message(String.format("Tu última lectura fue %.0f mg/dL, por encima de tu objetivo de %s mg/dL.",
                            value, patient.getTargetGlucoseMax()))
                    .build());
        }

        LocalDateTime weekAgo = now.minusDays(7);
        List<GlucoseReading> weekReadings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(), weekAgo, now);

        if (weekReadings.size() >= alertConfig.minReadingsForStats()) {
            BigDecimal avg = medicalCalculatorService.calculateAverage(weekReadings);
            BigDecimal hba1c = medicalCalculatorService.estimateHba1c(avg);

            if (hba1c.doubleValue() > 8.0) {
                alerts.add(Alert.builder()
                        .type(Alert.AlertType.HIGH_HBA1C_ESTIMATED)
                        .severity(Alert.Severity.WARNING)
                        .title("HbA1c estimada elevada")
                        .message(String.format("Tu HbA1c estimada es %.1f%%. Considera consultar a tu médico.",
                                hba1c.doubleValue()))
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
                    .title("Sin comidas registradas hoy")
                    .message("No has registrado ninguna comida hoy. El seguimiento nutricional mejora el control glucémico.")
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
                    .title("¡Excelente control glucémico!")
                    .message(String.format("Tu tiempo en rango de los últimos %d días es %.0f%%. ¡Sigue así!",
                            alertConfig.streakDays(), tir.doubleValue()))
                    .build());
        }

        return alerts;
    }

    private List<Alert> checkMenstrualCycleAlert(Patient patient) {
        List<Alert> alerts = new ArrayList<>();

        loadMenstrualCyclePort.findLatestByPatientId(patient.getPatientId())
                .ifPresent(cycle -> {
                    CyclePhase phase = cycle.calculateCurrentPhase(java.time.LocalDate.now());
                    long daysUntilNext = java.time.temporal.ChronoUnit.DAYS.between(
                            java.time.LocalDate.now(), cycle.predictNextCycleStart());

                    switch (phase) {
                        case LUTEAL_LATE -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title("⚡ Fase lútea tardía — Mayor resistencia a insulina")
                                .message(cycle.getPhaseGlucoseGuidance())
                                .build());
                        case LUTEAL_EARLY -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title("🌙 Fase lútea — Monitoreo frecuente recomendado")
                                .message(cycle.getPhaseGlucoseGuidance())
                                .build());
                        case OVULATION -> alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title("✨ Período de ovulación")
                                .message(cycle.getPhaseGlucoseGuidance())
                                .build());
                        default -> {}
                    }

                    if (daysUntilNext == 3) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.INFO)
                                .title("🩸 Tu período llega en 3 días")
                                .message("Prepárate para posibles cambios en tu glucosa.")
                                .build());
                    } else if (daysUntilNext == 1) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title("🩸 Tu período llega mañana")
                                .message("Monitorea tu glucosa con mayor frecuencia hoy y mañana.")
                                .build());
                    } else if (daysUntilNext == 0) {
                        alerts.add(Alert.builder()
                                .type(Alert.AlertType.GLUCOSE_AVERAGE_HIGH)
                                .severity(Alert.Severity.WARNING)
                                .title("🩸 Tu período comienza hoy")
                                .message("No olvides registrar el inicio de tu nuevo ciclo.")
                                .build());
                    }
                });

        return alerts;
    }
}