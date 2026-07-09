package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SendGlucoseReminderNotificationsUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadGlucoseReminderPort;
import com.diabecare.application.port.out.NotifyPatientPort;
import com.diabecare.domain.model.GlucoseReminder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SendGlucoseReminderNotificationsUseCaseImpl implements SendGlucoseReminderNotificationsUseCase {

    private static final String TITLE = "🩸 Hora de medir tu glucosa";
    private static final long SUPPRESS_IF_MEASURED_WITHIN_MINUTES = 30;

    private final LoadGlucoseReminderPort loadGlucoseReminderPort;
    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final NotifyPatientPort notifyPatientPort;
    private final Clock clock;

    @Override
    public void execute() {
        LocalTime now = LocalTime.now(clock).withSecond(0).withNano(0);

        for (GlucoseReminder reminder : loadGlucoseReminderPort.findAllEnabledAtTime(now)) {
            if (alreadyMeasuredRecently(reminder.getPatientId())) {
                log.debug("Paciente {} ya midió su glucosa recientemente, se omite el recordatorio",
                        reminder.getPatientId());
                continue;
            }

            String message = reminder.getLabel() != null && !reminder.getLabel().isBlank()
                    ? "Recordatorio: " + reminder.getLabel()
                    : "Es hora de registrar tu glucosa";

            notifyPatientPort.notify(reminder.getPatientId(), TITLE, message);
        }
    }

    private boolean alreadyMeasuredRecently(UUID patientId) {
        return loadGlucoseReadingPort.findLatestByPatientId(patientId)
                .map(reading -> ChronoUnit.MINUTES.between(reading.getMeasuredAt(), LocalDateTime.now(clock))
                        < SUPPRESS_IF_MEASURED_WITHIN_MINUTES)
                .orElse(false);
    }
}
