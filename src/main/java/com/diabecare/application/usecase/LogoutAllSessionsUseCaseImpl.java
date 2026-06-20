package com.diabecare.application.usecase;

import com.diabecare.application.port.in.LogoutAllSessionsUseCase;
import com.diabecare.application.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LogoutAllSessionsUseCaseImpl implements LogoutAllSessionsUseCase {

    private final RefreshTokenPort refreshTokenPort;

    @Override
    public void execute(Command command) {
        refreshTokenPort.revokeAllForUser(command.userId());
    }
}