package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetActiveSessionsUseCase;
import com.diabecare.application.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetActiveSessionsUseCaseImpl implements GetActiveSessionsUseCase {

    private final RefreshTokenPort refreshTokenPort;

    @Override
    public List<RefreshTokenPort.ActiveSession> execute(UUID userId) {
        return refreshTokenPort.findActiveSessions(userId);
    }
}