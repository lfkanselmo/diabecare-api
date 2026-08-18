package com.diabecare.application.port.in;

import com.diabecare.domain.model.MobilePlatform;

import java.util.UUID;

public interface RegisterMobileTokenUseCase {

    record Command(UUID patientId, String deviceToken, MobilePlatform platform) {}

    void execute(Command command);
}
