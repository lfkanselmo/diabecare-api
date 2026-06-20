package com.diabecare.application.port.in;

import com.diabecare.application.port.out.RefreshTokenPort;

import java.util.List;
import java.util.UUID;

public interface GetActiveSessionsUseCase {
    List<RefreshTokenPort.ActiveSession> execute(UUID userId);
}