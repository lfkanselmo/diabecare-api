package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SyncMedicationsUseCase;
import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.domain.model.Medication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SyncMedicationsUseCaseImpl implements SyncMedicationsUseCase {

    private static final LocalDateTime BEGINNING_OF_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final LoadMedicationPort loadMedicationPort;

    @Override
    public List<Medication> execute(UUID patientId, LocalDateTime since) {
        return loadMedicationPort.findByPatientIdUpdatedAfter(
                patientId, since != null ? since : BEGINNING_OF_TIME);
    }
}
