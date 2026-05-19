package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetPatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPatientUseCaseImpl implements GetPatientUseCase {

    private final LoadPatientPort loadPatientPort;

    @Override
    public Patient getByUserId(UUID userId) {
        return loadPatientPort.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException(userId.toString()));
    }
}