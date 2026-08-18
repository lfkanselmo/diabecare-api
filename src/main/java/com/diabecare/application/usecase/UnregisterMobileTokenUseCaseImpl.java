package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UnregisterMobileTokenUseCase;
import com.diabecare.application.port.out.MobilePushTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UnregisterMobileTokenUseCaseImpl implements UnregisterMobileTokenUseCase {

    private final MobilePushTokenPort mobilePushTokenPort;

    @Override
    public void execute(Command command) {
        mobilePushTokenPort.deleteByDeviceToken(command.deviceToken());
    }
}
