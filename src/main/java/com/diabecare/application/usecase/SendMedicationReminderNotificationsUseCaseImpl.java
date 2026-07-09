package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SendMedicationReminderNotificationsUseCase;
import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.application.port.out.NotifyPatientPort;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.service.MedicationReminderTimeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalTime;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SendMedicationReminderNotificationsUseCaseImpl implements SendMedicationReminderNotificationsUseCase {

    private static final String TITLE = "💊 Hora de tu medicamento";

    private final LoadMedicationPort loadMedicationPort;
    private final MedicationReminderTimeResolver reminderTimeResolver;
    private final NotifyPatientPort notifyPatientPort;
    private final Clock clock;

    @Override
    public void execute() {
        LocalTime now = LocalTime.now(clock).withSecond(0).withNano(0);

        for (Medication medication : loadMedicationPort.findAllActive()) {
            if (reminderTimeResolver.resolveTimes(medication.getFrequency()).contains(now)) {
                notifyPatientPort.notify(
                        medication.getPatientId(),
                        TITLE,
                        "Es hora de tomar " + medication.getName() + " (" +
                                medication.getDose() + " " + medication.getDoseUnit() + ")"
                );
                log.debug("Recordatorio de medicamento enviado: {} a paciente {}",
                        medication.getName(), medication.getPatientId());
            }
        }
    }
}
