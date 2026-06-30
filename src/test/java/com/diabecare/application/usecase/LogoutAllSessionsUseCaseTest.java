package com.diabecare.application.usecase;

import com.diabecare.application.port.in.LogoutAllSessionsUseCase;
import com.diabecare.application.port.out.RefreshTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutAllSessionsUseCaseImpl")
class LogoutAllSessionsUseCaseTest {

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @InjectMocks
    private LogoutAllSessionsUseCaseImpl useCase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("revoca todas las sesiones del usuario indicado")
        void revokesAllSessionsForUser() {
            UUID userId = UUID.randomUUID();

            useCase.execute(new LogoutAllSessionsUseCase.Command(userId));

            verify(refreshTokenPort).revokeAllForUser(userId);
            verifyNoMoreInteractions(refreshTokenPort);
        }
    }
}