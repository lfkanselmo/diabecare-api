package com.diabecare.application.port.in;

import java.util.UUID;

public interface RevokeDeviceApiKeyUseCase {

    record Command(UUID patientId, UUID keyId) {}

    void execute(Command command);
}
