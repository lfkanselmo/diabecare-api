package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMobileTokenUseCase;
import com.diabecare.application.port.out.MobilePushTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterMobileTokenUseCaseImpl implements RegisterMobileTokenUseCase {

    private final MobilePushTokenPort mobilePushTokenPort;

    @Override
    public void execute(Command command) {
        if (mobilePushTokenPort.existsByPatientIdAndDeviceToken(command.patientId(), command.deviceToken())) {
            return;
        }
        mobilePushTokenPort.save(command.patientId(), command.deviceToken(), command.platform());
    }
}
