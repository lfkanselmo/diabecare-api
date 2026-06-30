package com.diabecare.infrastructure.push;

import com.diabecare.application.port.out.PushSubscriptionPort;
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
    private final DiabeCareProperties properties;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public void subscribe(UUID patientId, String endpoint, String p256dh, String auth) {
        if (subscriptionPort.existsByPatientIdAndEndpoint(patientId, endpoint)) {
            return;
        }
        subscriptionPort.save(patientId, endpoint, p256dh, auth);
    }

    public void unsubscribe(String endpoint) {
        subscriptionPort.deleteByEndpoint(endpoint);
    }

    public void sendToPatient(UUID patientId, String title, String body) {
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

    protected PushService buildPushService() throws java.security.GeneralSecurityException {
        return new PushService(
                properties.push().vapidPublicKey(),
                properties.push().vapidPrivateKey(),
                properties.push().vapidSubject()
        );
    }
}