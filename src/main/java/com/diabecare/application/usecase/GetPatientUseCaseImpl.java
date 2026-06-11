package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetPatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPatientUseCaseImpl implements GetPatientUseCase {

    private final LoadPatientPort loadPatientPort;

    @Override
    public Result getById(UUID patientId) {
        return loadPatientPort.findById(patientId)
                .map(Result::new)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
    }

    @Override
    public Result getByUserId(UUID userId) {
        return loadPatientPort.findByUserId(userId)
                .map(Result::new)
                .orElseThrow(() -> new PatientNotFoundException(userId.toString()));
    }
}