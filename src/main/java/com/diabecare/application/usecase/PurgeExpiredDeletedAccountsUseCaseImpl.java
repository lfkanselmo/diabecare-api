package com.diabecare.application.usecase;

import com.diabecare.application.port.in.PurgeExpiredDeletedAccountsUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.PurgeAccountDataPort;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurgeExpiredDeletedAccountsUseCaseImpl implements PurgeExpiredDeletedAccountsUseCase {

    // Ley 1581 de 2012 (Habeas Data): ventana de gracia antes de la purga
    // definitiva, para permitir revertir una eliminación accidental.
    private static final int GRACE_PERIOD_DAYS = 30;

    private final LoadUserPort loadUserPort;
    private final LoadPatientPort loadPatientPort;
    private final PurgeAccountDataPort purgeAccountDataPort;

    @Override
    public int execute() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(GRACE_PERIOD_DAYS);
        List<UUID> expiredUserIds = loadUserPort.findIdsDeletedBefore(cutoff);

        for (UUID userId : expiredUserIds) {
            UUID patientId = loadPatientPort.findByUserId(userId)
                    .map(Patient::getPatientId)
                    .orElse(null);

            purgeAccountDataPort.purgeAllDataFor(userId, patientId);
        }

        return expiredUserIds.size();
    }
}
