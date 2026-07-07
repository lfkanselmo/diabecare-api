package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.domain.model.User;
import com.diabecare.infrastructure.persistence.entity.UserEntity;
import com.diabecare.infrastructure.persistence.repository.UserJpaRepository;
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
@DisplayName("UserPersistenceAdapter")
class UserPersistenceAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    private UserPersistenceAdapter adapter;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new UserPersistenceAdapter(userJpaRepository);
    }

    @Nested
    @DisplayName("findUserIdByEmail")
    class FindUserIdByEmail {

        @Test
        @DisplayName("retorna el id cuando el correo existe")
        void returnsIdWhenEmailExists() {
            UserEntity entity = validEntity();
            when(userJpaRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(entity));

            Optional<UUID> result = adapter.findUserIdByEmail("ana@example.com");

            assertThat(result).contains(entity.getId());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("mapea correctamente todos los campos de auditoría a User")
        void mapsAllAuditFieldsToUserCorrectly() {
            UserEntity entity = validEntity();
            when(userJpaRepository.findById(userId)).thenReturn(Optional.of(entity));

            Optional<User> result = adapter.findById(userId);

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("ana@example.com");
            assertThat(result.get().isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("guarda el usuario habilitado por defecto y retorna el UserRecord")
        void savesUserEnabledByDefaultAndReturnsUserRecord() {
            when(userJpaRepository.save(any())).thenAnswer(inv -> {
                UserEntity e = inv.getArgument(0);
                e.setId(userId);
                return e;
            });

            LocalDateTime termsAcceptedAt = LocalDateTime.now();
            UserRecord result = adapter.save("ana@example.com", "hashed-password", "PATIENT", termsAcceptedAt, "2026-07");

            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.email()).isEqualTo("ana@example.com");
            assertThat(result.role()).isEqualTo("PATIENT");

            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userJpaRepository).save(captor.capture());
            assertThat(captor.getValue().isEnabled()).isTrue();
            assertThat(captor.getValue().getPassword()).isEqualTo("hashed-password");
            assertThat(captor.getValue().getTermsAcceptedAt()).isEqualTo(termsAcceptedAt);
            assertThat(captor.getValue().getTermsVersion()).isEqualTo("2026-07");
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("delega la verificación al repositorio")
        void delegatesCheckToRepository() {
            when(userJpaRepository.existsByEmail("ana@example.com")).thenReturn(true);

            boolean result = adapter.existsByEmail("ana@example.com");

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("suspend")
    class Suspend {

        @Test
        @DisplayName("deshabilita el usuario y marca suspendedAt cuando existe")
        void disablesUserAndMarksSuspendedAtWhenExists() {
            UserEntity entity = validEntity();
            when(userJpaRepository.findById(userId)).thenReturn(Optional.of(entity));

            adapter.suspend(domainUser());

            assertThat(entity.isEnabled()).isFalse();
            assertThat(entity.getSuspendedAt()).isNotNull();
            verify(userJpaRepository).save(entity);
        }

        @Test
        @DisplayName("no hace nada cuando el usuario no existe")
        void doesNothingWhenUserDoesNotExist() {
            when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

            adapter.suspend(domainUser());

            verify(userJpaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deshabilita el usuario, marca deletedAt, y anonimiza el correo cuando existe")
        void disablesMarksDeletedAtAndAnonymizesEmailWhenExists() {
            UserEntity entity = validEntity();
            when(userJpaRepository.findById(userId)).thenReturn(Optional.of(entity));

            adapter.delete(domainUser());

            assertThat(entity.isEnabled()).isFalse();
            assertThat(entity.getDeletedAt()).isNotNull();
            assertThat(entity.getEmail()).isEqualTo("deleted_" + userId + "@diabecare.deleted");
            verify(userJpaRepository).save(entity);
        }

        @Test
        @DisplayName("no hace nada cuando el usuario no existe")
        void doesNothingWhenUserDoesNotExist() {
            when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

            adapter.delete(domainUser());

            verify(userJpaRepository, never()).save(any());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private UserEntity validEntity() {
        return UserEntity.builder()
                .id(userId)
                .email("ana@example.com")
                .password("hashed-password")
                .role("PATIENT")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private User domainUser() {
        return User.builder()
                .id(userId)
                .email("ana@example.com")
                .role("PATIENT")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}