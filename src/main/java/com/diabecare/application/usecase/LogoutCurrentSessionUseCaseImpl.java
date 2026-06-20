package com.diabecare.application.usecase;

import com.diabecare.application.port.in.LogoutCurrentSessionUseCase;
import com.diabecare.application.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LogoutCurrentSessionUseCaseImpl implements LogoutCurrentSessionUseCase {

    private final RefreshTokenPort refreshTokenPort;

    @Override
    public void execute(Command command) {
        refreshTokenPort.revokeOne(command.refreshToken());
    }
}