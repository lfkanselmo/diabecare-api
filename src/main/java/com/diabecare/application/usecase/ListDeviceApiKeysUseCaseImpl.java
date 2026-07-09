package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ListDeviceApiKeysUseCase;
import com.diabecare.application.port.out.DeviceApiKeyPort;
import com.diabecare.domain.model.DeviceApiKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ListDeviceApiKeysUseCaseImpl implements ListDeviceApiKeysUseCase {

    private final DeviceApiKeyPort deviceApiKeyPort;

    @Override
    public List<DeviceApiKey> execute(UUID patientId) {
        return deviceApiKeyPort.findAllByPatientId(patientId);
    }
}
