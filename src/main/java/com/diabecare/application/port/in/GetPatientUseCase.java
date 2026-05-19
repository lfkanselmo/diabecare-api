package com.diabecare.application.port.in;

import com.diabecare.domain.model.Patient;

import java.util.UUID;

public interface GetPatientUseCase {
    Patient getByUserId(UUID userId);
}