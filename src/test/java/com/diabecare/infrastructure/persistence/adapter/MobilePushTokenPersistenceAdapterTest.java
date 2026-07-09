package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.MobilePlatform;
import com.diabecare.domain.model.MobilePushToken;
import com.diabecare.infrastructure.persistence.entity.MobilePushTokenEntity;
import com.diabecare.infrastructure.persistence.mapper.MobilePushTokenPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.MobilePushTokenJpaRepository;
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
@DisplayName("MobilePushTokenPersistenceAdapter")
class MobilePushTokenPersistenceAdapterTest {

    @Mock
    private MobilePushTokenJpaRepository jpaRepository;

    private MobilePushTokenPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new MobilePushTokenPersistenceAdapter(jpaRepository, new MobilePushTokenPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("existsByPatientIdAndDeviceToken")
    class ExistsByPatientIdAndDeviceToken {

        @Test
        @DisplayName("retorna true cuando algún token del paciente coincide")
        void returnsTrueWhenSomeTokenMatches() {
            when(jpaRepository.findAllByPatientId(patientId))
                    .thenReturn(List.of(entityWithToken("token-1"), entityWithToken("token-2")));

            boolean result = adapter.existsByPatientIdAndDeviceToken(patientId, "token-2");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("retorna false cuando ningún token coincide")
        void returnsFalseWhenNoTokenMatches() {
            when(jpaRepository.findAllByPatientId(patientId))
                    .thenReturn(List.of(entityWithToken("token-1")));

            boolean result = adapter.existsByPatientIdAndDeviceToken(patientId, "token-99");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("construye y persiste la entidad con los datos correctos")
        void buildsAndPersistsEntityWithCorrectData() {
            adapter.save(patientId, "token-1", MobilePlatform.IOS);

            ArgumentCaptor<MobilePushTokenEntity> captor = ArgumentCaptor.forClass(MobilePushTokenEntity.class);
            verify(jpaRepository).save(captor.capture());

            assertThat(captor.getValue().getPatientId()).isEqualTo(patientId);
            assertThat(captor.getValue().getDeviceToken()).isEqualTo("token-1");
            assertThat(captor.getValue().getPlatform()).isEqualTo("IOS");
        }
    }

    @Nested
    @DisplayName("deleteByDeviceToken")
    class DeleteByDeviceToken {

        @Test
        @DisplayName("delega la eliminación al repositorio")
        void delegatesDeletionToRepository() {
            adapter.deleteByDeviceToken("token-1");

            verify(jpaRepository).deleteByDeviceToken("token-1");
        }
    }

    @Nested
    @DisplayName("findAllByPatientId")
    class FindAllByPatientId {

        @Test
        @DisplayName("retorna todos los tokens del paciente convertidos a dominio")
        void returnsAllPatientTokensConvertedToDomain() {
            when(jpaRepository.findAllByPatientId(patientId))
                    .thenReturn(List.of(entityWithToken("token-1")));

            List<MobilePushToken> result = adapter.findAllByPatientId(patientId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDeviceToken()).isEqualTo("token-1");
            assertThat(result.get(0).getPlatform()).isEqualTo(MobilePlatform.ANDROID);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private MobilePushTokenEntity entityWithToken(String token) {
        return MobilePushTokenEntity.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .deviceToken(token)
                .platform("ANDROID")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
