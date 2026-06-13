package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SendWeeklySummaryUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.NotifyPatientPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.model.WeeklySummaryData;
import com.diabecare.domain.service.WeeklySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendWeeklySummaryUseCaseImpl implements SendWeeklySummaryUseCase {

    private final LoadPatientPort        loadPatientPort;
    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final WeeklySummaryService   weeklySummaryService;
    private final NotifyPatientPort      notifyPatientPort;

    @Override
    public void sendToAllPatients() {
        LocalDateTime to   = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);

        loadPatientPort.findAll().forEach(patient ->
                processSummaryForPatient(patient, from, to));
    }

    private void processSummaryForPatient(Patient patient, LocalDateTime from, LocalDateTime to) {
        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patient.getPatientId(), from, to);

        weeklySummaryService.buildSummary(patient, readings).ifPresentOrElse(
                summary -> sendNotification(summary),
                () -> log.debug("Sin lecturas para paciente {}, omitiendo resumen",
                        patient.getPatientId())
        );
    }

    private void sendNotification(WeeklySummaryData summary) {
        notifyPatientPort.notify(
                summary.patientId(),
                weeklySummaryService.buildPushTitle(),
                weeklySummaryService.buildPushMessage(summary)
        );
        log.info("Resumen semanal enviado a {}", summary.patientName());
    }
}