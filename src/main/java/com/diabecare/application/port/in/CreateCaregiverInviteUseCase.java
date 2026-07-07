package com.diabecare.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CreateCaregiverInviteUseCase {

    record Result(String code, LocalDateTime expiresAt) {}

    Result execute(UUID patientId);
}
