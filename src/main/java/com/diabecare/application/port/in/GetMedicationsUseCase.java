package com.diabecare.application.port.in;

import com.diabecare.domain.model.Medication;

import java.util.List;
import java.util.UUID;

public interface GetMedicationsUseCase {
    List<Medication> getActiveByPatientId(UUID patientId);
}