package com.diabecare.application.usecase;

import com.diabecare.application.port.out.DeviceApiKeyPort;
import com.diabecare.domain.model.DeviceApiKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListDeviceApiKeysUseCaseImpl")
class ListDeviceApiKeysUseCaseTest {

    @Mock
    private DeviceApiKeyPort deviceApiKeyPort;

    private ListDeviceApiKeysUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ListDeviceApiKeysUseCaseImpl(deviceApiKeyPort);
    }

    @Test
    @DisplayName("retorna las keys del paciente tal como las entrega el puerto")
    void returnsPatientKeysAsProvidedByPort() {
        DeviceApiKey key = DeviceApiKey.builder().id(UUID.randomUUID()).patientId(patientId).label("Dexcom").build();
        when(deviceApiKeyPort.findAllByPatientId(patientId)).thenReturn(List.of(key));

        List<DeviceApiKey> result = useCase.execute(patientId);

        assertThat(result).containsExactly(key);
    }

    @Test
    @DisplayName("retorna lista vacía cuando el paciente no tiene keys")
    void returnsEmptyListWhenPatientHasNoKeys() {
        when(deviceApiKeyPort.findAllByPatientId(patientId)).thenReturn(List.of());

        List<DeviceApiKey> result = useCase.execute(patientId);

        assertThat(result).isEmpty();
    }
}
