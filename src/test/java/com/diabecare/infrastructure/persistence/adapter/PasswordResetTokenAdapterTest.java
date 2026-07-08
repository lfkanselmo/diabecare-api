package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.PasswordResetTokenPort;
import com.diabecare.domain.model.PasswordResetToken;
import com.diabecare.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.diabecare.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetTokenAdapter")
class PasswordResetTokenAdapterTest {

    @Mock
    private PasswordResetTokenJpaRepository repository;

    private PasswordResetTokenAdapter adapter;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new PasswordResetTokenAdapter(repository);
    }

    @Nested
    @DisplayName("issue")
    class Issue {

        @Test
        @DisplayName("genera un token crudo cuyo hash no coincide con el valor almacenado")
        void generatesRawTokenDifferentFromStoredHash() {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PasswordResetTokenPort.IssuedToken result = adapter.issue(userId, expiresAt);

            assertThat(result.rawToken()).isNotBlank();
            assertThat(result.expiresAt()).isEqualTo(expiresAt);

            ArgumentCaptor<PasswordResetTokenEntity> captor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getTokenHash()).isNotEqualTo(result.rawToken());
            assertThat(captor.getValue().getUserId()).isEqualTo(userId);
            assertThat(captor.getValue().getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("genera tokens distintos en cada llamada")
        void generatesDifferentTokensOnEachCall() {
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PasswordResetTokenPort.IssuedToken first = adapter.issue(userId, LocalDateTime.now().plusHours(1));
            PasswordResetTokenPort.IssuedToken second = adapter.issue(userId, LocalDateTime.now().plusHours(1));

            assertThat(first.rawToken()).isNotEqualTo(second.rawToken());
        }
    }

    @Nested
    @DisplayName("findByRawToken")
    class FindByRawToken {

        @Test
        @DisplayName("encuentra el token cuando su hash coincide")
        void findsTokenWhenHashMatches() {
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            PasswordResetTokenPort.IssuedToken issued = adapter.issue(userId, LocalDateTime.now().plusHours(1));

            ArgumentCaptor<PasswordResetTokenEntity> captor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
            verify(repository).save(captor.capture());
            when(repository.findByTokenHash(captor.getValue().getTokenHash()))
                    .thenReturn(Optional.of(captor.getValue()));

            Optional<PasswordResetToken> result = adapter.findByRawToken(issued.rawToken());

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el token no existe")
        void returnsEmptyWhenTokenDoesNotExist() {
            when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

            Optional<PasswordResetToken> result = adapter.findByRawToken("token-inexistente");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markUsed")
    class MarkUsed {

        @Test
        @DisplayName("marca el token como usado cuando existe")
        void marksTokenAsUsedWhenExists() {
            UUID tokenId = UUID.randomUUID();
            PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder().id(tokenId).build();

            when(repository.findById(tokenId)).thenReturn(Optional.of(entity));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.markUsed(tokenId);

            assertThat(entity.getUsedAt()).isNotNull();
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("no hace nada cuando el token no existe")
        void doesNothingWhenTokenDoesNotExist() {
            UUID tokenId = UUID.randomUUID();
            when(repository.findById(tokenId)).thenReturn(Optional.empty());

            adapter.markUsed(tokenId);

            verify(repository, never()).save(any());
        }
    }
}
