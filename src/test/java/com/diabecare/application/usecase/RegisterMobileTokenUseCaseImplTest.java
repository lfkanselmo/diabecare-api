package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMobileTokenUseCase;
import com.diabecare.application.port.out.MobilePushTokenPort;
import com.diabecare.domain.model.MobilePlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterMobileTokenUseCaseImpl")
class RegisterMobileTokenUseCaseImplTest {

    @Mock
    private MobilePushTokenPort mobilePushTokenPort;

    private final UUID patientId = UUID.randomUUID();
    private RegisterMobileTokenUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterMobileTokenUseCaseImpl(mobilePushTokenPort);
    }

    @Test
    @DisplayName("guarda el token cuando no existe previamente para ese dispositivo")
    void savesTokenWhenNotExisting() {
        when(mobilePushTokenPort.existsByPatientIdAndDeviceToken(patientId, "token-1")).thenReturn(false);

        useCase.execute(new RegisterMobileTokenUseCase.Command(patientId, "token-1", MobilePlatform.ANDROID));

        verify(mobilePushTokenPort).save(patientId, "token-1", MobilePlatform.ANDROID);
    }

    @Test
    @DisplayName("no guarda un token duplicado para el mismo paciente y dispositivo")
    void doesNotSaveDuplicateToken() {
        when(mobilePushTokenPort.existsByPatientIdAndDeviceToken(patientId, "token-1")).thenReturn(true);

        useCase.execute(new RegisterMobileTokenUseCase.Command(patientId, "token-1", MobilePlatform.ANDROID));

        verify(mobilePushTokenPort, never()).save(any(), any(), any());
    }
}
