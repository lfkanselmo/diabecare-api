package com.diabecare.application.usecase;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.application.port.in.RegisterPatientUseCase;
import com.diabecare.application.port.in.RegisterUseCase;
import com.diabecare.application.port.in.RegisterUserUseCase;
import com.diabecare.application.port.out.GenerateTokenPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.RateLimitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUseCaseImpl")
class RegisterUseCaseTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;
    @Mock
    private RegisterPatientUseCase registerPatientUseCase;
    @Mock
    private GenerateTokenPort generateTokenPort;
    @Mock
    private RefreshTokenPort refreshTokenPort;
    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private RegisterUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("orquesta el registro completo y retorna el resultado correcto")
        void orchestratesCompleteRegistration() {
            UserRecord userRecord = new UserRecord(userId, "ana@example.com", "PATIENT");
            Patient patient = validPatient();

            when(registerUserUseCase.execute(any())).thenReturn(userRecord);
            when(registerPatientUseCase.execute(any())).thenReturn(patient);
            when(generateTokenPort.generateToken("ana@example.com", userId)).thenReturn("access-token");
            when(generateTokenPort.getExpiresIn()).thenReturn(3600L);
            when(refreshTokenPort.issue(userId, "iPhone"))
                    .thenReturn(new RefreshTokenPort.IssuedToken("refresh-token", 604800000L));

            RegisterUseCase.Command command = new RegisterUseCase.Command(
                    "ana@example.com", "password123", "Ana García",
                    LocalDate.of(1990, 5, 10), DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1),
                    BigDecimal.valueOf(165), BiologicalSex.FEMALE, "iPhone", "127.0.0.1", "2026-07");

            RegisterUseCase.Result result = useCase.execute(command);

            assertThat(result.token()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.patientId()).isEqualTo(patient.getPatientId().toString());
            assertThat(result.userId()).isEqualTo(userId.toString());
            assertThat(result.role()).isEqualTo("PATIENT");
        }

        @Test
        @DisplayName("propaga el email y password correctos al registrar el usuario")
        void propagatesEmailAndPasswordToUserRegistration() {
            stubHappyPath();

            RegisterUseCase.Command command = new RegisterUseCase.Command(
                    "ana@example.com", "password123", "Ana García",
                    LocalDate.of(1990, 5, 10), DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1),
                    BigDecimal.valueOf(165), BiologicalSex.FEMALE, "iPhone", "127.0.0.1", "2026-07");

            useCase.execute(command);

            ArgumentCaptor<RegisterUserUseCase.Command> captor =
                    ArgumentCaptor.forClass(RegisterUserUseCase.Command.class);
            verify(registerUserUseCase).execute(captor.capture());

            assertThat(captor.getValue().email()).isEqualTo("ana@example.com");
            assertThat(captor.getValue().password()).isEqualTo("password123");
        }

        @Test
        @DisplayName("propaga el id del usuario recién creado y los datos del paciente al registrar el perfil")
        void propagatesUserIdAndPatientDataToPatientRegistration() {
            stubHappyPath();

            RegisterUseCase.Command command = new RegisterUseCase.Command(
                    "ana@example.com", "password123", "Ana García",
                    LocalDate.of(1990, 5, 10), DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1),
                    BigDecimal.valueOf(165), BiologicalSex.FEMALE, "iPhone", "127.0.0.1", "2026-07");

            useCase.execute(command);

            ArgumentCaptor<RegisterPatientUseCase.Command> captor =
                    ArgumentCaptor.forClass(RegisterPatientUseCase.Command.class);
            verify(registerPatientUseCase).execute(captor.capture());

            assertThat(captor.getValue().userId()).isEqualTo(userId);
            assertThat(captor.getValue().fullName()).isEqualTo("Ana García");
            assertThat(captor.getValue().diabetesType()).isEqualTo(DiabetesType.TYPE_1);
            assertThat(captor.getValue().biologicalSex()).isEqualTo(BiologicalSex.FEMALE);
        }

        @Test
        @DisplayName("no registra el perfil de paciente si el registro de usuario falla")
        void doesNotRegisterPatientIfUserRegistrationFails() {
            when(registerUserUseCase.execute(any()))
                    .thenThrow(new RuntimeException("correo ya registrado"));

            RegisterUseCase.Command command = new RegisterUseCase.Command(
                    "ana@example.com", "password123", "Ana García",
                    LocalDate.of(1990, 5, 10), DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1),
                    BigDecimal.valueOf(165), BiologicalSex.FEMALE, "iPhone", "127.0.0.1", "2026-07");

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(registerPatientUseCase, generateTokenPort, refreshTokenPort);
        }
    }


    private void stubHappyPath() {
        UserRecord userRecord = new UserRecord(userId, "ana@example.com", "PATIENT");
        Patient patient = validPatient();

        when(registerUserUseCase.execute(any())).thenReturn(userRecord);
        when(registerPatientUseCase.execute(any())).thenReturn(patient);
        when(generateTokenPort.generateToken(any(), any())).thenReturn("access-token");
        when(generateTokenPort.getExpiresIn()).thenReturn(3600L);
        when(refreshTokenPort.issue(any(), any()))
                .thenReturn(new RefreshTokenPort.IssuedToken("refresh-token", 604800000L));
    }

    private Patient validPatient() {
        return Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}
