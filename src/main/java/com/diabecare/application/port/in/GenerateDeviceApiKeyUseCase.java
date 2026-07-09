package com.diabecare.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GenerateDeviceApiKeyUseCase {

    record Command(UUID patientId, String label) {}

    record Result(UUID id, String rawKey, String label, LocalDateTime createdAt) {}

    Result execute(Command command);
}
