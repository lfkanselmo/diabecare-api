package com.diabecare.infrastructure.push;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationAdapter")
class PushNotificationAdapterTest {

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private PushNotificationAdapter adapter;

    @Test
    @DisplayName("delega la notificación al servicio con los mismos parámetros")
    void delegatesNotificationToServiceWithSameParameters() {
        UUID patientId = UUID.randomUUID();

        adapter.notify(patientId, "Título", "Mensaje");

        verify(pushNotificationService).sendToPatient(patientId, "Título", "Mensaje");
    }
}