package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SubscribeToPushUseCase;
import com.diabecare.application.port.out.PushSubscriptionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscribeToPushUseCaseImpl")
class SubscribeToPushUseCaseImplTest {

    @Mock
    private PushSubscriptionPort subscriptionPort;

    private final UUID patientId = UUID.randomUUID();
    private SubscribeToPushUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubscribeToPushUseCaseImpl(subscriptionPort);
    }

    @Test
    @DisplayName("guarda la suscripción cuando no existe previamente para ese endpoint")
    void savesSubscriptionWhenNotExisting() {
        when(subscriptionPort.existsByPatientIdAndEndpoint(patientId, "endpoint-1")).thenReturn(false);

        useCase.execute(new SubscribeToPushUseCase.Command(patientId, "endpoint-1", "p256dh-key", "auth-key"));

        verify(subscriptionPort).save(patientId, "endpoint-1", "p256dh-key", "auth-key");
    }

    @Test
    @DisplayName("no guarda una suscripción duplicada para el mismo paciente y endpoint")
    void doesNotSaveDuplicateSubscription() {
        when(subscriptionPort.existsByPatientIdAndEndpoint(patientId, "endpoint-1")).thenReturn(true);

        useCase.execute(new SubscribeToPushUseCase.Command(patientId, "endpoint-1", "p256dh-key", "auth-key"));

        verify(subscriptionPort, never()).save(any(), any(), any(), any());
    }
}
