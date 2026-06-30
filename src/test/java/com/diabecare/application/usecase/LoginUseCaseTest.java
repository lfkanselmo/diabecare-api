package com.diabecare.application.usecase;

import com.diabecare.application.port.in.LoginUseCase;
import com.diabecare.application.port.out.AuthenticateUserPort;
import com.diabecare.application.port.out.GenerateTokenPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCaseImpl")
class LoginUseCaseTest {

    @Mock
    private AuthenticateUserPort authenticateUserPort;
    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private GenerateTokenPort generateTokenPort;
    @Mock
    private RefreshTokenPort refreshTokenPort;

    @InjectMocks
    private LoginUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();
    private final String email = "ana@example.com";

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("retorna el resultado completo cuando todo es válido")
        void returnsCompleteResultWhenValid() {
            Patient patient = validPatient();

            when(loadUserPort.findUserIdByEmail(email)).thenReturn(Optional.of(userId));
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.of(patient));
            when(generateTokenPort.generateToken(email, userId)).thenReturn("access-token");
            when(generateTokenPort.getExpiresIn()).thenReturn(3600L);
            when(refreshTokenPort.issue(userId, "iPhone"))
                    .thenReturn(new RefreshTokenPort.IssuedToken("refresh-token", 604800000L));

            LoginUseCase.Result result = useCase.execute(
                    new LoginUseCase.Command(email, "password123", "iPhone"));

            assertThat(result.token()).isEqualTo("access-token");
            assertThat(result.expiresIn()).isEqualTo(3600L);
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.refreshExpiresIn()).isEqualTo(604800000L);
            assertThat(result.patientId()).isEqualTo(patient.getPatientId().toString());
            assertThat(result.userId()).isEqualTo(userId.toString());
        }

        @Test
        @DisplayName("autentica las credenciales antes de cualquier otra operación")
        void authenticatesBeforeAnythingElse() {
            Patient patient = validPatient();
            when(loadUserPort.findUserIdByEmail(email)).thenReturn(Optional.of(userId));
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.of(patient));
            when(generateTokenPort.generateToken(any(), any())).thenReturn("token");
            when(refreshTokenPort.issue(any(), any()))
                    .thenReturn(new RefreshTokenPort.IssuedToken("rt", 1000L));

            useCase.execute(new LoginUseCase.Command(email, "password123", "iPhone"));

            verify(authenticateUserPort).authenticate(email, "password123");
        }

        @Test
        @DisplayName("propaga la excepción cuando las credenciales son inválidas, sin continuar el flujo")
        void propagatesExceptionWhenCredentialsInvalid() {
            doThrow(new RuntimeException("credenciales inválidas"))
                    .when(authenticateUserPort).authenticate(email, "wrong");

            assertThatThrownBy(() -> useCase.execute(
                    new LoginUseCase.Command(email, "wrong", "iPhone")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("credenciales inválidas");

            verifyNoInteractions(loadUserPort, loadPatientPort, generateTokenPort, refreshTokenPort);
        }

        @Test
        @DisplayName("lanza excepción cuando el usuario no existe")
        void throwsWhenUserNotFound() {
            when(loadUserPort.findUserIdByEmail(email)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(
                    new LoginUseCase.Command(email, "password123", "iPhone")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verifyNoInteractions(loadPatientPort, generateTokenPort, refreshTokenPort);
        }

        @Test
        @DisplayName("lanza excepción cuando el perfil de paciente no existe")
        void throwsWhenPatientProfileNotFound() {
            when(loadUserPort.findUserIdByEmail(email)).thenReturn(Optional.of(userId));
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(
                    new LoginUseCase.Command(email, "password123", "iPhone")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Perfil no encontrado");

            verifyNoInteractions(generateTokenPort, refreshTokenPort);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Patient validPatient() {
        return Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}