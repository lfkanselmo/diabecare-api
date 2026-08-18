package com.diabecare.infrastructure.push;

import com.diabecare.application.port.out.MobilePushTokenPort;
import com.diabecare.application.port.out.PushSubscriptionPort;
import com.diabecare.domain.model.MobilePushToken;
import com.diabecare.domain.model.PushSubscription;
import com.diabecare.infrastructure.config.DiabeCareProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushSubscriptionPort subscriptionPort;
    private final MobilePushTokenPort mobilePushTokenPort;
    private final DiabeCareProperties properties;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public void sendToPatient(UUID patientId, String title, String body) {
        sendToWebSubscriptions(patientId, title, body);
        sendToMobileDevices(patientId, title, body);
    }

    private void sendToWebSubscriptions(UUID patientId, String title, String body) {
        List<PushSubscription> subs = subscriptionPort.findAllByPatientId(patientId);
        if (subs.isEmpty()) return;

        String payload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/icons/icon-192x192.png\"}",
                title.replace("\"", "\\\""),
                body.replace("\"", "\\\"")
        );

        for (PushSubscription sub : subs) {
            try {
                PushService pushService = buildPushService();
                Subscription subscription = new Subscription(
                        sub.getEndpoint(),
                        new Subscription.Keys(sub.getP256dh(), sub.getAuth())
                );
                pushService.send(new Notification(subscription, payload));
            } catch (Exception e) {
                log.warn("Error enviando push a {}: {}", sub.getEndpoint(), e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("410")) {
                    subscriptionPort.deleteByEndpoint(sub.getEndpoint());
                }
            }
        }
    }

    /**
     * Envío nativo (FCM) a la futura app móvil — sin usuarios reales todavía, así
     * que no hay proyecto de Firebase configurado ({@code FCM_SERVICE_ACCOUNT_JSON}
     * vacío). Se degrada igual que {@code ResendEmailAdapter} sin API key: registra
     * a quién se le habría enviado y no intenta la llamada. Cuando exista un
     * proyecto de Firebase real, este método debe firmar un JWT con la service
     * account (OAuth2 bearer) y llamar a la FCM HTTP v1 API — mismo patrón
     * java.net.http ya usado en el resto del proyecto, sin agregar el SDK de
     * Firebase Admin solo para esto.
     */
    private void sendToMobileDevices(UUID patientId, String title, String body) {
        List<MobilePushToken> tokens = mobilePushTokenPort.findAllByPatientId(patientId);
        if (tokens.isEmpty()) return;

        String serviceAccountJson = properties.push().fcmServiceAccountJson();
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            log.warn("FCM_SERVICE_ACCOUNT_JSON no configurada; push móvil no enviado a {} dispositivo(s) " +
                    "del paciente {}.", tokens.size(), patientId);
            return;
        }

        log.warn("Envío FCM real no implementado todavía — {} dispositivo(s) no notificados.", tokens.size());
    }

    protected PushService buildPushService() throws java.security.GeneralSecurityException {
        return new PushService(
                properties.push().vapidPublicKey(),
                properties.push().vapidPrivateKey(),
                properties.push().vapidSubject()
        );
    }
}