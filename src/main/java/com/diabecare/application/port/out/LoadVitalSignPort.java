package com.diabecare.application.port.out;

import com.diabecare.domain.model.VitalSign;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadVitalSignPort {
    Optional<VitalSign> findById(UUID vitalId);
    Optional<VitalSign> findLatestByPatientId(UUID patientId);
    List<VitalSign> findByPatientId(UUID patientId);
}