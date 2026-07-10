package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RefreshAccessTokenUseCase;
import com.diabecare.application.port.out.GenerateTokenPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.domain.exception.InvalidRefreshTokenException;
import com.diabecare.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshAccessTokenUseCaseImpl")
class RefreshAccessTokenUseCaseTest {

    @Mock
    private RefreshTokenPort refreshTokenPort;
    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private GenerateTokenPort generateTokenPort;

    @InjectMocks
    private RefreshAccessTokenUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("retorna nuevos tokens cuando el refresh token y el usuario son válidos")
        void returnsNewTokensWhenValid() {
            when(refreshTokenPort.redeem("valid-refresh-token"))
                    .thenReturn(Optional.of(new RefreshTokenPort.RedeemedToken(userId, "iPhone")));
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(enabledUser()));
            when(generateTokenPort.generateToken("ana@example.com", userId)).thenReturn("new-access-token");
            when(generateTokenPort.getExpiresIn()).thenReturn(3600L);
            when(refreshTokenPort.issue(userId, "iPhone"))
                    .thenReturn(new RefreshTokenPort.IssuedToken("new-refresh-token", 604800000L));

            RefreshAccessTokenUseCase.Result result = useCase.execute(
                    new RefreshAccessTokenUseCase.Command("valid-refresh-token"));

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("emite el nuevo refresh token preservando el mismo dispositivo")
        void issuesNewRefreshTokenWithSameDevice() {
            when(refreshTokenPort.redeem(any()))
                    .thenReturn(Optional.of(new RefreshTokenPort.RedeemedToken(userId, "Android-Pixel")));
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(enabledUser()));
            when(generateTokenPort.generateToken(any(), any())).thenReturn("token");
            when(refreshTokenPort.issue(any(), any()))
                    .thenReturn(new RefreshTokenPort.IssuedToken("rt", 1000L));

            useCase.execute(new RefreshAccessTokenUseCase.Command("valid-refresh-token"));

            verify(refreshTokenPort).issue(userId, "Android-Pixel");
        }

        @Test
        @DisplayName("lanza InvalidRefreshTokenException cuando el token no se puede redimir")
        void throwsWhenTokenCannotBeRedeemed() {
            when(refreshTokenPort.redeem("invalid-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(
                    new RefreshAccessTokenUseCase.Command("invalid-token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            verifyNoInteractions(loadUserPort, generateTokenPort);
        }

        @Test
        @DisplayName("lanza InvalidRefreshTokenException cuando el usuario asociado no existe")
        void throwsWhenUserNotFound() {
            when(refreshTokenPort.redeem(any()))
                    .thenReturn(Optional.of(new RefreshTokenPort.RedeemedToken(userId, "iPhone")));
            when(loadUserPort.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(
                    new RefreshAccessTokenUseCase.Command("valid-refresh-token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            verifyNoInteractions(generateTokenPort);
        }

        @Test
        @DisplayName("lanza InvalidRefreshTokenException cuando el usuario está suspendido")
        void throwsWhenUserSuspended() {
            when(refreshTokenPort.redeem(any()))
                    .thenReturn(Optional.of(new RefreshTokenPort.RedeemedToken(userId, "iPhone")));
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(suspendedUser()));

            assertThatThrownBy(() -> useCase.execute(
                    new RefreshAccessTokenUseCase.Command("valid-refresh-token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            verifyNoInteractions(generateTokenPort);
        }

        @Test
        @DisplayName("lanza InvalidRefreshTokenException cuando el usuario fue eliminado")
        void throwsWhenUserDeleted() {
            when(refreshTokenPort.redeem(any()))
                    .thenReturn(Optional.of(new RefreshTokenPort.RedeemedToken(userId, "iPhone")));
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(deletedUser()));

            assertThatThrownBy(() -> useCase.execute(
                    new RefreshAccessTokenUseCase.Command("valid-refresh-token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            verifyNoInteractions(generateTokenPort);
        }

        @Test
        @DisplayName("lanza InvalidRefreshTokenException cuando el usuario está deshabilitado")
        void throwsWhenUserDisabled() {
            when(refreshTokenPort.redeem(any()))
                    .thenReturn(Optional.of(new RefreshTokenPort.RedeemedToken(userId, "iPhone")));
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(disabledUser()));

            assertThatThrownBy(() -> useCase.execute(
                    new RefreshAccessTokenUseCase.Command("valid-refresh-token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            verifyNoInteractions(generateTokenPort);
        }
    }


    private User enabledUser() {
        return User.builder()
                .id(userId).email("ana@example.com").role("PATIENT")
                .enabled(true).createdAt(LocalDateTime.now())
                .build();
    }

    private User suspendedUser() {
        return User.builder()
                .id(userId).email("ana@example.com").role("PATIENT")
                .enabled(true).suspendedAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                .build();
    }

    private User deletedUser() {
        return User.builder()
                .id(userId).email("ana@example.com").role("PATIENT")
                .enabled(true).deletedAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                .build();
    }

    private User disabledUser() {
        return User.builder()
                .id(userId).email("ana@example.com").role("PATIENT")
                .enabled(false).createdAt(LocalDateTime.now())
                .build();
    }
}
