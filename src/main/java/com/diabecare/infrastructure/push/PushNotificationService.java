package com.diabecare.infrastructure.push;

import com.diabecare.infrastructure.config.DiabeCareProperties;
import com.diabecare.infrastructure.persistence.entity.PushSubscriptionEntity;
import com.diabecare.infrastructure.persistence.repository.PushSubscriptionJpaRepository;
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

    private final PushSubscriptionJpaRepository subscriptionRepository;
    private final DiabeCareProperties properties;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public void subscribe(UUID patientId, String endpoint, String p256dh, String auth) {
        if (subscriptionRepository.findAllByPatientId(patientId)
                .stream().anyMatch(s -> s.getEndpoint().equals(endpoint))) {
            return;
        }
        subscriptionRepository.save(PushSubscriptionEntity.builder()
                .patientId(patientId)
                .endpoint(endpoint)
                .p256dh(p256dh)
                .auth(auth)
                .build());
    }

    public void unsubscribe(String endpoint) {
        subscriptionRepository.deleteByEndpoint(endpoint);
    }

    public void sendToPatient(UUID patientId, String title, String body) {
        List<PushSubscriptionEntity> subs = subscriptionRepository.findAllByPatientId(patientId);
        if (subs.isEmpty()) return;

        String payload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/icons/icon-192x192.png\"}",
                title.replace("\"", "\\\""),
                body.replace("\"", "\\\"")
        );

        for (PushSubscriptionEntity sub : subs) {
            try {
                PushService pushService = new PushService(
                        properties.push().vapidPublicKey(),
                        properties.push().vapidPrivateKey(),
                        properties.push().vapidSubject()
                );
                Subscription subscription = new Subscription(
                        sub.getEndpoint(),
                        new Subscription.Keys(sub.getP256dh(), sub.getAuth())
                );
                pushService.send(new Notification(subscription, payload));
            } catch (Exception e) {
                log.warn("Error enviando push a {}: {}", sub.getEndpoint(), e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("410")) {
                    subscriptionRepository.deleteByEndpoint(sub.getEndpoint());
                }
            }
        }
    }
}