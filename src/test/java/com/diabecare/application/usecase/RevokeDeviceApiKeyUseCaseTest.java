package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RevokeDeviceApiKeyUseCase;
import com.diabecare.application.port.out.DeviceApiKeyPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RevokeDeviceApiKeyUseCaseImpl")
class RevokeDeviceApiKeyUseCaseTest {

    @Mock
    private DeviceApiKeyPort deviceApiKeyPort;

    private RevokeDeviceApiKeyUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();
    private final UUID keyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RevokeDeviceApiKeyUseCaseImpl(deviceApiKeyPort);
    }

    @Test
    @DisplayName("delega la revocación al puerto con el patientId y keyId correctos")
    void delegatesRevocationToPortWithCorrectPatientIdAndKeyId() {
        useCase.execute(new RevokeDeviceApiKeyUseCase.Command(patientId, keyId));

        verify(deviceApiKeyPort).revoke(patientId, keyId);
    }
}
