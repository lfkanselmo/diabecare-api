package com.diabecare.application.port.in;

import com.diabecare.domain.model.DeviceApiKey;

import java.util.List;
import java.util.UUID;

public interface ListDeviceApiKeysUseCase {
    List<DeviceApiKey> execute(UUID patientId);
}
