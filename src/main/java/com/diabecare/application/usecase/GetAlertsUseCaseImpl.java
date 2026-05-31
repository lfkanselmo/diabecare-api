package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetAlertsUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.MedicalCalculatorService;
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
    private final MedicalCalculatorService medicalCalculatorService;

    private static final int HOURS_WITHOUT_GLUCOSE = 8;
    private static final int STREAK_DAYS = 7;
    private static final double GOOD_TIR_THRESHOLD = 70.0;

    @Override
    public List<Alert> getAlerts(UUID patientId) {
        Patient patient = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));

        List<Alert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        alerts.addAll(checkGlucoseAlerts(patient, now));
        alerts.addAll(checkCalorieAlert(patient, now));
        alerts.addAll(checkPositiveStreak(patient, now));

        return alerts;
    }

    private List<Alert> checkGlucoseAlerts(Patient patient, LocalDateTime now) {
        List<Alert> alerts = new ArrayList<>();
        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(),
                        now.minusHours(HOURS_WITHOUT_GLUCOSE), now);

        if (readings.isEmpty()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.NO_GLUCOSE_RECORDED)
                    .severity(Alert.Severity.INFO)
                    .title("Sin registros recientes")
                    .message("No has registrado tu glucosa en las últimas " +
                            HOURS_WITHOUT_GLUCOSE + " horas.")
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

        if (weekReadings.size() >= 3) {
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

        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(),
                        now.minusDays(STREAK_DAYS), now);

        if (readings.size() < 5) return alerts;

        BigDecimal tir = medicalCalculatorService.calculateTimeInRange(
                readings, patient.getTargetGlucoseMin(), patient.getTargetGlucoseMax());

        if (tir.doubleValue() >= GOOD_TIR_THRESHOLD) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.POSITIVE_STREAK)
                    .severity(Alert.Severity.SUCCESS)
                    .title("¡Excelente control!")
                    .message(String.format("Tu tiempo en rango los últimos %d días es %.1f%%. " +
                            "¡Sigue así!", STREAK_DAYS, tir.doubleValue()))
                    .build());
        }

        return alerts;
    }
}