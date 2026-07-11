package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SyncVitalSignsUseCase;
import com.diabecare.application.port.out.LoadVitalSignPort;
import com.diabecare.domain.model.VitalSign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SyncVitalSignsUseCaseImpl implements SyncVitalSignsUseCase {

    private static final LocalDateTime BEGINNING_OF_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final LoadVitalSignPort loadVitalSignPort;

    @Override
    public List<VitalSign> execute(UUID patientId, LocalDateTime since) {
        return loadVitalSignPort.findByPatientIdUpdatedAfter(
                patientId, since != null ? since : BEGINNING_OF_TIME);
    }
}
