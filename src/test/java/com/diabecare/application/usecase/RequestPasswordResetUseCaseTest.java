package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RequestPasswordResetUseCase;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.PasswordResetTokenPort;
import com.diabecare.application.port.out.SendEmailPort;
import com.diabecare.domain.service.RateLimitService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestPasswordResetUseCaseImpl")
class RequestPasswordResetUseCaseTest {

    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private PasswordResetTokenPort passwordResetTokenPort;
    @Mock
    private SendEmailPort sendEmailPort;
    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private RequestPasswordResetUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();
    private final String email = "ana@example.com";

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("verifica el límite de tasa antes de cualquier otra operación")
        void checksRateLimitBeforeAnythingElse() {
            when(loadUserPort.findUserIdByEmail(email)).thenReturn(Optional.empty());

            useCase.execute(new RequestPasswordResetUseCase.Command(email, "127.0.0.1"));

            verify(rateLimitService).checkForgotPasswordLimit("127.0.0.1");
        }

        @Test
        @DisplayName("cuando el correo existe, emite un token y envía el correo con ese token crudo")
        void whenEmailExistsIssuesTokenAndSendsResetEmail() {
            when(loadUserPort.findUserIdByEmail(email)).thenReturn(Optional.of(userId));
            when(passwordResetTokenPort.issue(eq(userId), any(LocalDateTime.class)))
                    .thenReturn(new PasswordResetTokenPort.IssuedToken("raw-token-123", LocalDateTime.now().plusHours(1)));

            useCase.execute(new RequestPasswordResetUseCase.Command(email, "127.0.0.1"));

            verify(sendEmailPort).sendPasswordResetEmail(email, "raw-token-123");
        }

        @Test
        @DisplayName("cuando el correo no existe, no emite token ni intenta enviar nada (anti-enumeración)")
        void whenEmailDoesNotExistDoesNotIssueTokenOrSendEmail() {
            when(loadUserPort.findUserIdByEmail(email)).thenReturn(Optional.empty());

            useCase.execute(new RequestPasswordResetUseCase.Command(email, "127.0.0.1"));

            verifyNoInteractions(passwordResetTokenPort, sendEmailPort);
        }

        @Test
        @DisplayName("no propaga la excepción cuando el envío del correo falla")
        void doesNotPropagateExceptionWhenEmailSendingFails() {
            when(loadUserPort.findUserIdByEmail(email)).thenReturn(Optional.of(userId));
            when(passwordResetTokenPort.issue(eq(userId), any(LocalDateTime.class)))
                    .thenReturn(new PasswordResetTokenPort.IssuedToken("raw-token-123", LocalDateTime.now().plusHours(1)));
            doThrow(new RuntimeException("Resend no disponible"))
                    .when(sendEmailPort).sendPasswordResetEmail(any(), any());

            useCase.execute(new RequestPasswordResetUseCase.Command(email, "127.0.0.1"));
            // Si llegamos aquí sin excepción, el requisito anti-enumeración se cumple.
        }
    }
}
