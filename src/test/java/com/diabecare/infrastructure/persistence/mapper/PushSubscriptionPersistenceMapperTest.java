package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.PushSubscription;
import com.diabecare.infrastructure.persistence.entity.PushSubscriptionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PushSubscriptionPersistenceMapper")
class PushSubscriptionPersistenceMapperTest {

    private final PushSubscriptionPersistenceMapper mapper = new PushSubscriptionPersistenceMapperImpl();

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente sin transformaciones")
        void mapsAllFieldsDirectly() {
            UUID id = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.now();

            PushSubscriptionEntity entity = PushSubscriptionEntity.builder()
                    .id(id)
                    .patientId(patientId)
                    .endpoint("https://fcm.example.com/abc123")
                    .p256dh("p256dh-value")
                    .auth("auth-value")
                    .createdAt(createdAt)
                    .build();

            PushSubscription subscription = mapper.toDomain(entity);

            assertThat(subscription.getId()).isEqualTo(id);
            assertThat(subscription.getPatientId()).isEqualTo(patientId);
            assertThat(subscription.getEndpoint()).isEqualTo("https://fcm.example.com/abc123");
            assertThat(subscription.getP256dh()).isEqualTo("p256dh-value");
            assertThat(subscription.getAuth()).isEqualTo("auth-value");
            assertThat(subscription.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}