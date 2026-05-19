package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetMedicationsUseCase;
import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.domain.model.Medication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMedicationsUseCaseImpl implements GetMedicationsUseCase {

    private final LoadMedicationPort loadMedicationPort;

    @Override
    public List<Medication> getActiveByPatientId(UUID patientId) {
        return loadMedicationPort.findActiveByPatientId(patientId);
    }
}