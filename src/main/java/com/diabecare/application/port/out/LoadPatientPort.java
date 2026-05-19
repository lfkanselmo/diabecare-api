package com.diabecare.application.port.out;

import com.diabecare.domain.model.Patient;

import java.util.Optional;
import java.util.UUID;

public interface LoadPatientPort {
    Optional<Patient> findById(UUID patientId);
    Optional<Patient> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}