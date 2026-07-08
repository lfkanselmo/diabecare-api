package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ResetPasswordUseCase;
import com.diabecare.application.port.out.PasswordResetTokenPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.exception.InvalidPasswordResetTokenException;
import com.diabecare.domain.model.PasswordResetToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResetPasswordUseCaseImpl")
class ResetPasswordUseCaseTest {

    @Mock
    private PasswordResetTokenPort passwordResetTokenPort;
    @Mock
    private SaveUserPort saveUserPort;
    @Mock
    private RefreshTokenPort refreshTokenPort;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordUseCaseImpl useCase;

    private final UUID tokenId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("actualiza la contraseña, marca el token como usado, y revoca todas las sesiones")
        void updatesPasswordMarksTokenUsedAndRevokesAllSessions() {
            PasswordResetToken token = validToken();
            when(passwordResetTokenPort.findByRawToken("raw-token")).thenReturn(Optional.of(token));
            when(passwordEncoder.encode("nueva-contraseña-123")).thenReturn("hash-nueva");

            useCase.execute(new ResetPasswordUseCase.Command("raw-token", "nueva-contraseña-123"));

            verify(saveUserPort).updatePassword(userId, "hash-nueva");
            verify(passwordResetTokenPort).markUsed(tokenId);
            verify(refreshTokenPort).revokeAllForUser(userId);
        }

        @Test
        @DisplayName("lanza InvalidPasswordResetTokenException cuando el token no existe")
        void throwsInvalidPasswordResetTokenExceptionWhenTokenDoesNotExist() {
            when(passwordResetTokenPort.findByRawToken("token-inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(
                    new ResetPasswordUseCase.Command("token-inexistente", "nueva-contraseña-123")))
                    .isInstanceOf(InvalidPasswordResetTokenException.class);

            verifyNoInteractions(saveUserPort, refreshTokenPort);
        }

        @Test
        @DisplayName("lanza InvalidPasswordResetTokenException cuando el token ya expiró")
        void throwsInvalidPasswordResetTokenExceptionWhenTokenExpired() {
            PasswordResetToken expiredToken = PasswordResetToken.builder()
                    .id(tokenId).userId(userId)
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .build();
            when(passwordResetTokenPort.findByRawToken("raw-token")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> useCase.execute(
                    new ResetPasswordUseCase.Command("raw-token", "nueva-contraseña-123")))
                    .isInstanceOf(InvalidPasswordResetTokenException.class);

            verifyNoInteractions(saveUserPort, refreshTokenPort);
        }

        @Test
        @DisplayName("lanza InvalidPasswordResetTokenException cuando el token ya fue usado")
        void throwsInvalidPasswordResetTokenExceptionWhenTokenAlreadyUsed() {
            PasswordResetToken usedToken = PasswordResetToken.builder()
                    .id(tokenId).userId(userId)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .usedAt(LocalDateTime.now().minusMinutes(5))
                    .build();
            when(passwordResetTokenPort.findByRawToken("raw-token")).thenReturn(Optional.of(usedToken));

            assertThatThrownBy(() -> useCase.execute(
                    new ResetPasswordUseCase.Command("raw-token", "nueva-contraseña-123")))
                    .isInstanceOf(InvalidPasswordResetTokenException.class);

            verifyNoInteractions(saveUserPort, refreshTokenPort);
        }
    }

    private PasswordResetToken validToken() {
        return PasswordResetToken.builder()
                .id(tokenId).userId(userId)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }
}
