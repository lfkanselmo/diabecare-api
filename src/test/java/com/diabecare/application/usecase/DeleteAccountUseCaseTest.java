package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
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
@DisplayName("DeleteAccountUseCaseImpl")
class DeleteAccountUseCaseTest {

    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private SaveUserPort saveUserPort;
    @Mock
    private RefreshTokenPort refreshTokenPort;

    @InjectMocks
    private DeleteAccountUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("elimina la cuenta y revoca todas las sesiones activas")
        void deletesAccountAndRevokesAllSessions() {
            User user = activeUser();
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(user));

            useCase.execute(userId);

            verify(saveUserPort).delete(user);
            verify(refreshTokenPort).revokeAllForUser(userId);
        }

        @Test
        @DisplayName("lanza excepción cuando el usuario no existe")
        void throwsWhenUserNotFound() {
            when(loadUserPort.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(userId))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining(userId.toString());

            verifyNoInteractions(saveUserPort, refreshTokenPort);
        }

        @Test
        @DisplayName("lanza excepción cuando la cuenta ya fue eliminada")
        void throwsWhenAccountAlreadyDeleted() {
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(deletedUser()));

            assertThatThrownBy(() -> useCase.execute(userId))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("ya fue eliminada");

            verifyNoInteractions(saveUserPort, refreshTokenPort);
        }
    }


    private User activeUser() {
        return User.builder()
                .id(userId).email("ana@example.com").role("PATIENT")
                .enabled(true).createdAt(LocalDateTime.now())
                .build();
    }

    private User deletedUser() {
        return User.builder()
                .id(userId).email("ana@example.com").role("PATIENT")
                .enabled(true).deletedAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                .build();
    }
}
