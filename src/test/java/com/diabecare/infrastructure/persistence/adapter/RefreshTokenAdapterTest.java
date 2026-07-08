package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.infrastructure.config.JwtProperties;
import com.diabecare.infrastructure.persistence.entity.RefreshTokenEntity;
import com.diabecare.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import com.diabecare.infrastructure.persistence.support.RefreshTokenReuseResponder;
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
@DisplayName("RefreshTokenAdapter")
class RefreshTokenAdapterTest {

    @Mock
    private RefreshTokenJpaRepository repository;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private RefreshTokenReuseResponder reuseResponder;

    private RefreshTokenAdapter adapter;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new RefreshTokenAdapter(repository, jwtProperties, reuseResponder);
    }

    @Nested
    @DisplayName("issue")
    class Issue {

        @Test
        @DisplayName("genera un token crudo distinto del hash almacenado, y respeta la expiración configurada")
        void generatesRawTokenDifferentFromStoredHashAndRespectsConfiguredExpiry() {
            when(jwtProperties.getRefreshTokenExpiryMs()).thenReturn(604_800_000L);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshTokenPort.IssuedToken result = adapter.issue(userId, "iPhone");

            assertThat(result.rawToken()).isNotBlank();
            assertThat(result.expiresInMs()).isEqualTo(604_800_000L);

            ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getTokenHash()).isNotEqualTo(result.rawToken());
            assertThat(captor.getValue().getUserId()).isEqualTo(userId);
            assertThat(captor.getValue().getDeviceLabel()).isEqualTo("iPhone");
        }

        @Test
        @DisplayName("genera tokens distintos en cada llamada")
        void generatesDifferentTokensOnEachCall() {
            when(jwtProperties.getRefreshTokenExpiryMs()).thenReturn(604_800_000L);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshTokenPort.IssuedToken first = adapter.issue(userId, "iPhone");
            RefreshTokenPort.IssuedToken second = adapter.issue(userId, "iPhone");

            assertThat(first.rawToken()).isNotEqualTo(second.rawToken());
        }
    }

    @Nested
    @DisplayName("redeem")
    class Redeem {

        @Test
        @DisplayName("redime correctamente un token válido, no revocado y no expirado, y lo marca como revocado")
        void redeemsValidTokenAndMarksAsRevoked() {
            RefreshTokenEntity entity = RefreshTokenEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .deviceLabel("iPhone")
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .revokedAt(null)
                    .build();

            when(repository.findByTokenHash(any())).thenReturn(Optional.of(entity));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Optional<RefreshTokenPort.RedeemedToken> result = adapter.redeem("raw-token-value");

            assertThat(result).isPresent();
            assertThat(result.get().userId()).isEqualTo(userId);
            assertThat(result.get().deviceLabel()).isEqualTo("iPhone");
            assertThat(entity.getRevokedAt()).isNotNull();
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el token no existe en la base de datos")
        void returnsEmptyWhenTokenDoesNotExist() {
            when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

            Optional<RefreshTokenPort.RedeemedToken> result = adapter.redeem("invalid-token");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el token ya fue revocado previamente")
        void returnsEmptyWhenTokenAlreadyRevoked() {
            RefreshTokenEntity entity = RefreshTokenEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .revokedAt(LocalDateTime.now().minusHours(1))
                    .build();

            when(repository.findByTokenHash(any())).thenReturn(Optional.of(entity));

            Optional<RefreshTokenPort.RedeemedToken> result = adapter.redeem("already-revoked-token");

            assertThat(result).isEmpty();
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("detecta reutilización de un token ya revocado y delega la revocación masiva "
                + "a una transacción independiente (REQUIRES_NEW) para que sobreviva el rollback "
                + "del InvalidRefreshTokenException que lanzará el caller")
        void detectsReuseOfAlreadyRevokedTokenAndRevokesAllUserSessions() {
            RefreshTokenEntity entity = RefreshTokenEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .revokedAt(LocalDateTime.now().minusHours(1))
                    .build();

            when(repository.findByTokenHash(any())).thenReturn(Optional.of(entity));

            adapter.redeem("stolen-and-already-rotated-token");

            verify(reuseResponder).revokeAllForUser(userId);
            verify(repository, never()).revokeAllActiveByUserId(any(), any());
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el token ya expiró")
        void returnsEmptyWhenTokenExpired() {
            RefreshTokenEntity entity = RefreshTokenEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .expiresAt(LocalDateTime.now().minusHours(1))
                    .revokedAt(null)
                    .build();

            when(repository.findByTokenHash(any())).thenReturn(Optional.of(entity));

            Optional<RefreshTokenPort.RedeemedToken> result = adapter.redeem("expired-token");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("revokeOne")
    class RevokeOne {

        @Test
        @DisplayName("marca el token como revocado cuando existe y no estaba revocado")
        void marksTokenAsRevokedWhenExistsAndNotAlreadyRevoked() {
            RefreshTokenEntity entity = RefreshTokenEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .revokedAt(null)
                    .build();

            when(repository.findByTokenHash(any())).thenReturn(Optional.of(entity));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.revokeOne("raw-token");

            assertThat(entity.getRevokedAt()).isNotNull();
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("no hace nada cuando el token no existe")
        void doesNothingWhenTokenDoesNotExist() {
            when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

            adapter.revokeOne("nonexistent-token");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("revokeAllForUser")
    class RevokeAllForUser {

        @Test
        @DisplayName("delega la revocación masiva al repositorio")
        void delegatesMassRevocationToRepository() {
            adapter.revokeAllForUser(userId);

            verify(repository).revokeAllActiveByUserId(eq(userId), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("findActiveSessions")
    class FindActiveSessions {

        @Test
        @DisplayName("retorna las sesiones activas convertidas correctamente")
        void returnsActiveSessionsConvertedCorrectly() {
            RefreshTokenEntity entity = RefreshTokenEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .deviceLabel("Android")
                    .lastUsedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();

            when(repository.findActiveByUserId(userId)).thenReturn(List.of(entity));

            List<RefreshTokenPort.ActiveSession> result = adapter.findActiveSessions(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).deviceLabel()).isEqualTo("Android");
        }
    }
}