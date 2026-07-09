package com.diabecare.infrastructure.scheduler;

import com.diabecare.application.port.in.SendGlucoseReminderNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GlucoseReminderScheduler {

    private final SendGlucoseReminderNotificationsUseCase sendGlucoseReminderNotificationsUseCase;

    @Scheduled(cron = "0 * * * * *", zone = "America/Bogota")
    public void sendGlucoseReminders() {
        sendGlucoseReminderNotificationsUseCase.execute();
    }
}
