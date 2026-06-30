package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.PushSubscription;
import com.diabecare.infrastructure.persistence.entity.PushSubscriptionEntity;
import com.diabecare.infrastructure.persistence.mapper.PushSubscriptionPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.PushSubscriptionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushSubscriptionPersistenceAdapter")
class PushSubscriptionPersistenceAdapterTest {

    @Mock
    private PushSubscriptionJpaRepository jpaRepository;

    private PushSubscriptionPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new PushSubscriptionPersistenceAdapter(jpaRepository, new PushSubscriptionPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("existsByPatientIdAndEndpoint")
    class ExistsByPatientIdAndEndpoint {

        @Test
        @DisplayName("retorna true cuando alguna suscripción del paciente coincide con el endpoint")
        void returnsTrueWhenSomeSubscriptionMatchesEndpoint() {
            when(jpaRepository.findAllByPatientId(patientId))
                    .thenReturn(List.of(entityWithEndpoint("endpoint-1"), entityWithEndpoint("endpoint-2")));

            boolean result = adapter.existsByPatientIdAndEndpoint(patientId, "endpoint-2");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("retorna false cuando ninguna suscripción coincide con el endpoint")
        void returnsFalseWhenNoSubscriptionMatchesEndpoint() {
            when(jpaRepository.findAllByPatientId(patientId))
                    .thenReturn(List.of(entityWithEndpoint("endpoint-1")));

            boolean result = adapter.existsByPatientIdAndEndpoint(patientId, "endpoint-99");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("retorna false cuando el paciente no tiene ninguna suscripción")
        void returnsFalseWhenPatientHasNoSubscriptions() {
            when(jpaRepository.findAllByPatientId(patientId)).thenReturn(List.of());

            boolean result = adapter.existsByPatientIdAndEndpoint(patientId, "endpoint-1");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("construye y persiste la entidad con los datos correctos")
        void buildsAndPersistsEntityWithCorrectData() {
            adapter.save(patientId, "endpoint-1", "p256dh-key", "auth-key");

            ArgumentCaptor<PushSubscriptionEntity> captor = ArgumentCaptor.forClass(PushSubscriptionEntity.class);
            verify(jpaRepository).save(captor.capture());

            assertThat(captor.getValue().getPatientId()).isEqualTo(patientId);
            assertThat(captor.getValue().getEndpoint()).isEqualTo("endpoint-1");
            assertThat(captor.getValue().getP256dh()).isEqualTo("p256dh-key");
            assertThat(captor.getValue().getAuth()).isEqualTo("auth-key");
        }
    }

    @Nested
    @DisplayName("deleteByEndpoint")
    class DeleteByEndpoint {

        @Test
        @DisplayName("delega la eliminación al repositorio")
        void delegatesDeletionToRepository() {
            adapter.deleteByEndpoint("endpoint-1");

            verify(jpaRepository).deleteByEndpoint("endpoint-1");
        }
    }

    @Nested
    @DisplayName("findAllByPatientId")
    class FindAllByPatientId {

        @Test
        @DisplayName("retorna todas las suscripciones del paciente convertidas a dominio")
        void returnsAllPatientSubscriptionsConvertedToDomain() {
            when(jpaRepository.findAllByPatientId(patientId))
                    .thenReturn(List.of(entityWithEndpoint("endpoint-1")));

            List<PushSubscription> result = adapter.findAllByPatientId(patientId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEndpoint()).isEqualTo("endpoint-1");
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private PushSubscriptionEntity entityWithEndpoint(String endpoint) {
        return PushSubscriptionEntity.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .endpoint(endpoint)
                .p256dh("p256dh-key")
                .auth("auth-key")
                .createdAt(LocalDateTime.now())
                .build();
    }
}