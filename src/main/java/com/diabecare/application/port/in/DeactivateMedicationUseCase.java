package com.diabecare.application.port.in;

import java.util.UUID;

public interface DeactivateMedicationUseCase {
    void execute(UUID medicationId, UUID patientId);
}