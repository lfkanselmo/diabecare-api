package com.diabecare.application.port.in;

import java.util.UUID;

public interface SubscribeToPushUseCase {

    record Command(UUID patientId, String endpoint, String p256dh, String auth) {}

    void execute(Command command);
}
