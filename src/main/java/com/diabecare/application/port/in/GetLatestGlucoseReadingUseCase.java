package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReading;

import java.util.Optional;
import java.util.UUID;

public interface GetLatestGlucoseReadingUseCase {
    Optional<GlucoseReading> getLatest(UUID patientId);
}