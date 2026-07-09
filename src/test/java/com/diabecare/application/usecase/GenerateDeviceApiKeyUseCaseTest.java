package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GenerateDeviceApiKeyUseCase;
import com.diabecare.application.port.out.DeviceApiKeyPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateDeviceApiKeyUseCaseImpl")
class GenerateDeviceApiKeyUseCaseTest {

    @Mock
    private DeviceApiKeyPort deviceApiKeyPort;

    private GenerateDeviceApiKeyUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GenerateDeviceApiKeyUseCaseImpl(deviceApiKeyPort);
    }

    @Test
    @DisplayName("emite la key vía el puerto y retorna la key cruda, id, label y fecha")
    void issuesKeyViaPortAndReturnsRawKeyIdLabelAndDate() {
        UUID keyId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        when(deviceApiKeyPort.issue(eq(patientId), eq("Glucómetro Bluetooth")))
                .thenReturn(new DeviceApiKeyPort.IssuedKey(keyId, "dbc_rawkey123", "Glucómetro Bluetooth", createdAt));

        GenerateDeviceApiKeyUseCase.Result result = useCase.execute(
                new GenerateDeviceApiKeyUseCase.Command(patientId, "Glucómetro Bluetooth"));

        assertThat(result.id()).isEqualTo(keyId);
        assertThat(result.rawKey()).isEqualTo("dbc_rawkey123");
        assertThat(result.label()).isEqualTo("Glucómetro Bluetooth");
        assertThat(result.createdAt()).isEqualTo(createdAt);
    }
}
