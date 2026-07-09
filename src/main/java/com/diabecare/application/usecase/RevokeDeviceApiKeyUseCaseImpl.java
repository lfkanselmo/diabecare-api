package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RevokeDeviceApiKeyUseCase;
import com.diabecare.application.port.out.DeviceApiKeyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RevokeDeviceApiKeyUseCaseImpl implements RevokeDeviceApiKeyUseCase {

    private final DeviceApiKeyPort deviceApiKeyPort;

    @Override
    public void execute(Command command) {
        deviceApiKeyPort.revoke(command.patientId(), command.keyId());
    }
}
