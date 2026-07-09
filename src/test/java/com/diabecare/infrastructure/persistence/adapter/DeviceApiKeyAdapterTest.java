package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.DeviceApiKeyPort;
import com.diabecare.domain.model.DeviceApiKey;
import com.diabecare.infrastructure.persistence.entity.DeviceApiKeyEntity;
import com.diabecare.infrastructure.persistence.repository.DeviceApiKeyJpaRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceApiKeyAdapter")
class DeviceApiKeyAdapterTest {

    @Mock
    private DeviceApiKeyJpaRepository repository;

    private DeviceApiKeyAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new DeviceApiKeyAdapter(repository);
    }

    @Nested
    @DisplayName("issue")
    class Issue {

        @Test
        @DisplayName("genera una key con prefijo dbc_ cuyo hash no coincide con la key cruda")
        void generatesKeyWithPrefixWhoseHashDiffersFromRawKey() {
            when(repository.save(any())).thenAnswer(inv -> {
                DeviceApiKeyEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            DeviceApiKeyPort.IssuedKey result = adapter.issue(patientId, "Dexcom G6");

            assertThat(result.rawKey()).startsWith("dbc_");
            assertThat(result.label()).isEqualTo("Dexcom G6");

            ArgumentCaptor<DeviceApiKeyEntity> captor = ArgumentCaptor.forClass(DeviceApiKeyEntity.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getKeyHash()).isNotEqualTo(result.rawKey());
            assertThat(captor.getValue().getPatientId()).isEqualTo(patientId);
        }

        @Test
        @DisplayName("genera keys distintas en cada llamada")
        void generatesDifferentKeysOnEachCall() {
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DeviceApiKeyPort.IssuedKey first = adapter.issue(patientId, "A");
            DeviceApiKeyPort.IssuedKey second = adapter.issue(patientId, "B");

            assertThat(first.rawKey()).isNotEqualTo(second.rawKey());
        }
    }

    @Nested
    @DisplayName("findByRawKey")
    class FindByRawKey {

        @Test
        @DisplayName("encuentra la key por su hash")
        void findsKeyByItsHash() {
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            DeviceApiKeyPort.IssuedKey issued = adapter.issue(patientId, "Dexcom G6");

            ArgumentCaptor<DeviceApiKeyEntity> captor = ArgumentCaptor.forClass(DeviceApiKeyEntity.class);
            verify(repository).save(captor.capture());
            when(repository.findByKeyHash(captor.getValue().getKeyHash()))
                    .thenReturn(Optional.of(captor.getValue()));

            Optional<DeviceApiKey> result = adapter.findByRawKey(issued.rawKey());

            assertThat(result).isPresent();
            assertThat(result.get().getPatientId()).isEqualTo(patientId);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando la key no existe")
        void returnsEmptyWhenKeyDoesNotExist() {
            when(repository.findByKeyHash(any())).thenReturn(Optional.empty());

            Optional<DeviceApiKey> result = adapter.findByRawKey("dbc_nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("revoke")
    class Revoke {

        @Test
        @DisplayName("marca la key como revocada cuando pertenece al paciente")
        void marksKeyAsRevokedWhenItBelongsToPatient() {
            UUID keyId = UUID.randomUUID();
            DeviceApiKeyEntity entity = DeviceApiKeyEntity.builder().id(keyId).patientId(patientId).build();
            when(repository.findById(keyId)).thenReturn(Optional.of(entity));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.revoke(patientId, keyId);

            assertThat(entity.getRevokedAt()).isNotNull();
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("no revoca cuando la key pertenece a otro paciente")
        void doesNotRevokeWhenKeyBelongsToAnotherPatient() {
            UUID keyId = UUID.randomUUID();
            DeviceApiKeyEntity entity = DeviceApiKeyEntity.builder()
                    .id(keyId).patientId(UUID.randomUUID()).build();
            when(repository.findById(keyId)).thenReturn(Optional.of(entity));

            adapter.revoke(patientId, keyId);

            assertThat(entity.getRevokedAt()).isNull();
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("touchLastUsed")
    class TouchLastUsed {

        @Test
        @DisplayName("actualiza lastUsedAt cuando la key existe")
        void updatesLastUsedAtWhenKeyExists() {
            UUID keyId = UUID.randomUUID();
            DeviceApiKeyEntity entity = DeviceApiKeyEntity.builder().id(keyId).build();
            when(repository.findById(keyId)).thenReturn(Optional.of(entity));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.touchLastUsed(keyId);

            assertThat(entity.getLastUsedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findAllByPatientId")
    class FindAllByPatientId {

        @Test
        @DisplayName("retorna todas las keys del paciente mapeadas al dominio")
        void returnsAllPatientKeysMappedToDomain() {
            DeviceApiKeyEntity entity = DeviceApiKeyEntity.builder()
                    .id(UUID.randomUUID()).patientId(patientId).label("Dexcom G6")
                    .createdAt(LocalDateTime.now()).build();
            when(repository.findAllByPatientIdOrderByCreatedAtDesc(patientId)).thenReturn(List.of(entity));

            List<DeviceApiKey> result = adapter.findAllByPatientId(patientId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLabel()).isEqualTo("Dexcom G6");
        }
    }
}
