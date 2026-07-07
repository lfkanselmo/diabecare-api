package com.diabecare.domain.service;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.Alert;
import com.diabecare.domain.model.CyclePhase;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Detecta alertas relacionadas con la fase del ciclo menstrual, la proximidad del
 * próximo período y períodos abiertos por demasiado tiempo. Solo aplica a pacientes
 * femeninas — cada detector decide su propia aplicabilidad, no el orquestador.
 */
@RequiredArgsConstructor
public class MenstrualCycleAlertDetector implements AlertDetector {

    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final CycleStatisticsService cycleStatisticsService;
    private final MessageResolverPort messages;
    private final MenstrualCycleGuidanceService cycleGuidanceService;
    private final AlertConfigPort alertConfig;

    @Override
    public List<Alert> detect(Patient patient, LocalDateTime now) {
        if (!patient.isFemale()) return List.of();

        return loadMenstrualCyclePort.findLatestByPatientId(patient.getPatientId())
                .map(cycle -> buildAlerts(patient, cycle))
                .orElseGet(List::of);
    }

    private List<Alert> buildAlerts(Patient patient, MenstrualCycle cycle) {
        List<Alert> alerts = new ArrayList<>();

        List<MenstrualCycle> history = loadMenstrualCyclePort.findByPatientId(patient.getPatientId());
        Integer avgCycleLength = cycleStatisticsService.calculateAverageCycleLength(history);
        Integer avgPeriodLength = cycleStatisticsService.calculateAveragePeriodLength(history);

        CyclePhase phase = cycle.calculateCurrentPhase(LocalDate.now(), avgCycleLength, avgPeriodLength);
        long daysUntilNext = ChronoUnit.DAYS.between(
                LocalDate.now(), cycle.predictNextCycleStart(LocalDate.now(), avgCycleLength));

        addPhaseAlert(alerts, phase);
        addUpcomingPeriodAlert(alerts, daysUntilNext);
        addOpenTooLongAlert(alerts, cycle);

        return alerts;
    }

    private void addPhaseAlert(List<Alert> alerts, CyclePhase phase) {
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
            default -> { }
        }
    }

    private void addUpcomingPeriodAlert(List<Alert> alerts, long daysUntilNext) {
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
    }

    private void addOpenTooLongAlert(List<Alert> alerts, MenstrualCycle cycle) {
        if (!cycle.isOngoing()) return;

        long daysSinceStart = ChronoUnit.DAYS.between(cycle.getStartDate(), LocalDate.now());

        if (daysSinceStart >= alertConfig.daysBeforeOpenCycleAlert()) {
            alerts.add(Alert.builder()
                    .type(Alert.AlertType.OPEN_CYCLE_REMINDER)
                    .severity(Alert.Severity.WARNING)
                    .title(messages.resolve("alert.cycle.open-too-long.title"))
                    .message(messages.resolve("alert.cycle.open-too-long.message", daysSinceStart))
                    .build());
        }
    }
}
