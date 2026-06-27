package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetLatestGlucoseReadingUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetLatestGlucoseReadingUseCaseImpl implements GetLatestGlucoseReadingUseCase {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;

    @Override
    public Optional<GlucoseReading> getLatest(UUID patientId) {
        return loadGlucoseReadingPort.findLatestByPatientId(patientId);
    }
}