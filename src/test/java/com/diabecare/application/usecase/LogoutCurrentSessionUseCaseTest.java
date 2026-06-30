package com.diabecare.application.usecase;

import com.diabecare.application.port.in.LogoutCurrentSessionUseCase;
import com.diabecare.application.port.out.RefreshTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutCurrentSessionUseCaseImpl")
class LogoutCurrentSessionUseCaseTest {

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @InjectMocks
    private LogoutCurrentSessionUseCaseImpl useCase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("revoca únicamente el refresh token de la sesión actual")
        void revokesOnlyCurrentSessionToken() {
            useCase.execute(new LogoutCurrentSessionUseCase.Command("refresh-token-123"));

            verify(refreshTokenPort).revokeOne("refresh-token-123");
            verifyNoMoreInteractions(refreshTokenPort);
        }
    }
}