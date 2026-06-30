package com.diabecare.application.usecase;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.application.port.in.RegisterUserUseCase;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCaseImpl")
class RegisterUserUseCaseTest {

    @Mock
    private SaveUserPort saveUserPort;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCaseImpl useCase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("registra el usuario con el password encriptado y rol PATIENT")
        void registersUserWithEncodedPasswordAndPatientRole() {
            UserRecord expected = new UserRecord(UUID.randomUUID(), "ana@example.com", "PATIENT");

            when(saveUserPort.existsByEmail("ana@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
            when(saveUserPort.save("ana@example.com", "hashed-password", "PATIENT")).thenReturn(expected);

            UserRecord result = useCase.execute(
                    new RegisterUserUseCase.Command("ana@example.com", "password123"));

            assertThat(result).isEqualTo(expected);
            verify(saveUserPort).save("ana@example.com", "hashed-password", "PATIENT");
        }

        @Test
        @DisplayName("rechaza el registro cuando ya existe una cuenta con el mismo correo")
        void rejectsWhenEmailAlreadyExists() {
            when(saveUserPort.existsByEmail("ana@example.com")).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute(
                    new RegisterUserUseCase.Command("ana@example.com", "password123")))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("ana@example.com");

            verifyNoInteractions(passwordEncoder);
            verify(saveUserPort, never()).save(any(), any(), any());
        }

        @Test
        @DisplayName("nunca guarda el password en texto plano")
        void neverSavesPlaintextPassword() {
            when(saveUserPort.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
            when(saveUserPort.save(any(), any(), any()))
                    .thenReturn(new UserRecord(UUID.randomUUID(), "ana@example.com", "PATIENT"));

            useCase.execute(new RegisterUserUseCase.Command("ana@example.com", "password123"));

            verify(saveUserPort, never()).save(any(), eq("password123"), any());
            verify(saveUserPort).save(any(), eq("hashed-password"), any());
        }
    }
}