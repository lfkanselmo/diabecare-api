package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetVitalSignsUseCase;
import com.diabecare.application.port.out.LoadVitalSignPort;
import com.diabecare.domain.model.VitalSign;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetVitalSignsUseCaseImpl implements GetVitalSignsUseCase {

    private final LoadVitalSignPort loadVitalSignPort;

    @Override
    public Page<VitalSign> getByPatientId(UUID patientId, Pageable pageable) {
        return loadVitalSignPort.findByPatientId(patientId, pageable);
    }

    @Override
    public Optional<VitalSign> getLatest(UUID patientId) {
        return loadVitalSignPort.findLatestByPatientId(patientId);
    }
}