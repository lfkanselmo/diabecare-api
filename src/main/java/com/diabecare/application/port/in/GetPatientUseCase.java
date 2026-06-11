package com.diabecare.application.port.in;

import com.diabecare.domain.model.Patient;

import java.util.UUID;

public interface GetPatientUseCase {

    Result getById(UUID patientId);
    Result getByUserId(UUID userId);

    record Result(Patient patient) {}
}