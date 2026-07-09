package com.diabecare.infrastructure.scheduler;

import com.diabecare.application.port.in.SendMedicationReminderNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicationReminderScheduler {

    private final SendMedicationReminderNotificationsUseCase sendMedicationReminderNotificationsUseCase;

    @Scheduled(cron = "0 * * * * *", zone = "America/Bogota")
    public void sendMedicationReminders() {
        sendMedicationReminderNotificationsUseCase.execute();
    }
}
