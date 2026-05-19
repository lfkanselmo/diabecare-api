package com.diabecare.application.port.in;

import com.diabecare.domain.model.VitalSign;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetVitalSignsUseCase {
    List<VitalSign> getByPatientId(UUID patientId);
    Optional<VitalSign> getLatest(UUID patientId);
}