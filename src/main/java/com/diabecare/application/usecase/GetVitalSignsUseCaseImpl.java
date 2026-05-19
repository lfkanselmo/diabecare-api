package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetVitalSignsUseCase;
import com.diabecare.application.port.out.LoadVitalSignPort;
import com.diabecare.domain.model.VitalSign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetVitalSignsUseCaseImpl implements GetVitalSignsUseCase {

    private final LoadVitalSignPort loadVitalSignPort;

    @Override
    public List<VitalSign> getByPatientId(UUID patientId) {
        return loadVitalSignPort.findByPatientId(patientId);
    }

    @Override
    public Optional<VitalSign> getLatest(UUID patientId) {
        return loadVitalSignPort.findLatestByPatientId(patientId);
    }
}