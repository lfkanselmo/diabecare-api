package com.diabecare.application.port.in;

import java.util.UUID;

public interface RedeemCaregiverInviteUseCase {

    record Command(String code, UUID caregiverUserId) {}

    record Result(UUID patientId, String patientFullName) {}

    Result execute(Command command);
}
