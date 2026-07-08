package com.diabecare.application.port.in;

import com.diabecare.domain.model.VitalSign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GetVitalSignsUseCase {
    Page<VitalSign> getByPatientId(UUID patientId, Pageable pageable);
    Optional<VitalSign> getLatest(UUID patientId);
}