package com.diabecare.infrastructure.scheduler;

import com.diabecare.application.port.in.PurgeExpiredDeletedAccountsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountPurgeScheduler {

    private final PurgeExpiredDeletedAccountsUseCase purgeExpiredDeletedAccountsUseCase;

    @Scheduled(cron = "0 0 3 * * *", zone = "America/Bogota")
    public void purgeExpiredDeletedAccounts() {
        log.info("Iniciando purga de cuentas eliminadas hace más de 30 días");
        int purged = purgeExpiredDeletedAccountsUseCase.execute();
        log.info("Purga completada: {} cuenta(s) purgada(s) definitivamente", purged);
    }
}
