package com.diabecare.infrastructure.push;

import com.diabecare.application.port.out.MobilePushTokenPort;
import com.diabecare.application.port.out.PushSubscriptionPort;
import com.diabecare.domain.model.MobilePlatform;
import com.diabecare.domain.model.MobilePushToken;
import com.diabecare.domain.model.PushSubscription;
import com.diabecare.infrastructure.config.DiabeCareProperties;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationService")
class PushNotificationServiceTest {

    @Mock
    private PushSubscriptionPort subscriptionPort;
    @Mock
    private MobilePushTokenPort mobilePushTokenPort;
    @Mock
    private DiabeCareProperties properties;
    @Mock
    private PushService mockPushService;

    private final UUID patientId = UUID.randomUUID();
    private PushNotificationService service;

    @BeforeEach
    void setUp() {
        service = new PushNotificationService(subscriptionPort, mobilePushTokenPort, properties) {
            @Override
            protected PushService buildPushService() {
                return mockPushService;
            }
        };
    }

    @Nested
    @DisplayName("sendToPatient")
    class SendToPatient {

        @Test
        @DisplayName("no intenta enviar push movil cuando el paciente no tiene tokens registrados")
        void doesNotAttemptMobilePushWhenNoTokens() {
            when(subscriptionPort.findAllByPatientId(patientId)).thenReturn(List.of());
            when(mobilePushTokenPort.findAllByPatientId(patientId)).thenReturn(List.of());

            assertThatCode(() -> service.sendToPatient(patientId, "Título", "Mensaje"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("no lanza excepción cuando hay tokens móviles pero FCM no está configurado")
        void doesNotThrowWhenMobileTokensExistButFcmNotConfigured() {
            when(subscriptionPort.findAllByPatientId(patientId)).thenReturn(List.of());
            when(mobilePushTokenPort.findAllByPatientId(patientId)).thenReturn(List.of(
                    MobilePushToken.builder().id(UUID.randomUUID()).patientId(patientId)
                            .deviceToken("token-1").platform(MobilePlatform.ANDROID)
                            .createdAt(LocalDateTime.now()).build()));
            when(properties.push()).thenReturn(new DiabeCareProperties.Push(null, null, null, null));

            assertThatCode(() -> service.sendToPatient(patientId, "Título", "Mensaje"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("no hace nada cuando el paciente no tiene suscripciones")
        void doesNothingWhenNoSubscriptions() {
            when(subscriptionPort.findAllByPatientId(patientId)).thenReturn(List.of());

            service.sendToPatient(patientId, "Título", "Mensaje");

            verifyNoInteractions(mockPushService);
        }

        @Test
        @DisplayName("envía la notificación a cada suscripción activa del paciente")
        void sendsNotificationToEachActiveSubscription() throws Exception {
            PushSubscription sub1 = subscriptionWith("endpoint-1");
            PushSubscription sub2 = subscriptionWith("endpoint-2");
            when(subscriptionPort.findAllByPatientId(patientId)).thenReturn(List.of(sub1, sub2));
            when(mockPushService.send(any())).thenReturn(mock(HttpResponse.class));

            service.sendToPatient(patientId, "Título", "Mensaje");

            verify(mockPushService, times(2)).send(any(Notification.class));
        }

        @Test
        @DisplayName("no lanza excepción aunque el título o el cuerpo contengan comillas dobles")
        void doesNotThrowWhenTitleOrBodyContainDoubleQuotes() throws Exception {
            PushSubscription sub = subscriptionWith("endpoint-1");
            when(subscriptionPort.findAllByPatientId(patientId)).thenReturn(List.of(sub));
            when(mockPushService.send(any())).thenReturn(mock(HttpResponse.class));

            assertThatCode(() ->
                    service.sendToPatient(patientId, "Título con \"comillas\"", "Mensaje normal"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("elimina la suscripción cuando el envío falla con código 410 (expirada)")
        void deletesSubscriptionWhenSendFailsWith410() throws Exception {
            PushSubscription sub = subscriptionWith("endpoint-expired");
            when(subscriptionPort.findAllByPatientId(patientId)).thenReturn(List.of(sub));
            when(mockPushService.send(any()))
                    .thenThrow(new GeneralSecurityException("Push service responded with 410 Gone"));

            service.sendToPatient(patientId, "Título", "Mensaje");

            verify(subscriptionPort).deleteByEndpoint("endpoint-expired");
        }

        @Test
        @DisplayName("no elimina la suscripción cuando el envío falla por un error distinto a 410")
        void doesNotDeleteSubscriptionWhenSendFailsWithOtherError() throws Exception {
            PushSubscription sub = subscriptionWith("endpoint-1");
            when(subscriptionPort.findAllByPatientId(patientId)).thenReturn(List.of(sub));
            when(mockPushService.send(any()))
                    .thenThrow(new GeneralSecurityException("Connection timeout"));

            service.sendToPatient(patientId, "Título", "Mensaje");

            verify(subscriptionPort, never()).deleteByEndpoint(any());
        }

        @Test
        @DisplayName("continúa enviando a las demás suscripciones aunque una falle")
        void continuesSendingToOthersWhenOneFails() throws Exception {
            PushSubscription failing = subscriptionWith("endpoint-failing");
            PushSubscription working = subscriptionWith("endpoint-working");
            when(subscriptionPort.findAllByPatientId(patientId)).thenReturn(List.of(failing, working));
            when(mockPushService.send(any()))
                    .thenThrow(new GeneralSecurityException("error"))
                    .thenReturn(mock(HttpResponse.class));

            service.sendToPatient(patientId, "Título", "Mensaje");

            verify(mockPushService, times(2)).send(any());
        }
    }


    // Punto EC P-256 sin comprimir real (65 bytes), válido para Subscription.Keys.
    // auth: 16 bytes aleatorios válidos según el protocolo Web Push.
    private static final String VALID_P256DH =
            "BNsmkOFoRYnBTZ4aOcU9iP0SBK72wGiyolHA5UcrgxCEgFl9212mXIL6x-Q9-WMFKMDPiSvW642pHN4ploqsYXc";
    private static final String VALID_AUTH = "M6GtwGax7CqGj7DlWrFv_g";

    private PushSubscription subscriptionWith(String endpoint) {
        return PushSubscription.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .endpoint(endpoint)
                .p256dh(VALID_P256DH)
                .auth(VALID_AUTH)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
