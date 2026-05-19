package com.diabecare.application.port.in;

import java.util.UUID;

public interface DeleteGlucoseReadingUseCase {
    void execute(UUID readingId, UUID patientId);
}