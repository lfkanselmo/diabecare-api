package com.diabecare.infrastructure.scheduler;

import com.diabecare.application.port.in.SendWeeklySummaryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklySummaryScheduler {

    private final SendWeeklySummaryUseCase sendWeeklySummaryUseCase;

    @Scheduled(cron = "0 0 8 * * MON", zone = "America/Bogota")
    public void sendWeeklySummaries() {
        log.info("Iniciando envío de resúmenes semanales");
        sendWeeklySummaryUseCase.sendToAllPatients();
        log.info("Resúmenes semanales completados");
    }
}