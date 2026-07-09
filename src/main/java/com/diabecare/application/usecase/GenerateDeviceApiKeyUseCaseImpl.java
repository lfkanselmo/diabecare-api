package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GenerateDeviceApiKeyUseCase;
import com.diabecare.application.port.out.DeviceApiKeyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GenerateDeviceApiKeyUseCaseImpl implements GenerateDeviceApiKeyUseCase {

    private final DeviceApiKeyPort deviceApiKeyPort;

    @Override
    public Result execute(Command command) {
        DeviceApiKeyPort.IssuedKey issued = deviceApiKeyPort.issue(command.patientId(), command.label());
        return new Result(issued.id(), issued.rawKey(), issued.label(), issued.createdAt());
    }
}
