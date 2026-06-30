package com.diabecare.application.usecase;

import com.diabecare.application.port.out.RefreshTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetActiveSessionsUseCaseImpl")
class GetActiveSessionsUseCaseTest {

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @InjectMocks
    private GetActiveSessionsUseCaseImpl useCase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("retorna la lista de sesiones activas del usuario")
        void returnsActiveSessionsForUser() {
            UUID userId = UUID.randomUUID();
            var session = new RefreshTokenPort.ActiveSession(
                    UUID.randomUUID(), "iPhone", LocalDateTime.now(), LocalDateTime.now().minusDays(1));

            when(refreshTokenPort.findActiveSessions(userId)).thenReturn(List.of(session));

            List<RefreshTokenPort.ActiveSession> result = useCase.execute(userId);

            assertThat(result).containsExactly(session);
        }

        @Test
        @DisplayName("retorna lista vacía cuando el usuario no tiene sesiones activas")
        void returnsEmptyListWhenNoActiveSessions() {
            UUID userId = UUID.randomUUID();
            when(refreshTokenPort.findActiveSessions(userId)).thenReturn(List.of());

            List<RefreshTokenPort.ActiveSession> result = useCase.execute(userId);

            assertThat(result).isEmpty();
        }
    }
}