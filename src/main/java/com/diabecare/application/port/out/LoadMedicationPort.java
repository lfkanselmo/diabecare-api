package com.diabecare.application.port.out;

import com.diabecare.domain.model.Medication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadMedicationPort {
    Optional<Medication> findById(UUID medicationId);
    List<Medication> findActiveByPatientId(UUID patientId);
    List<Medication> findAllByPatientId(UUID patientId);
}