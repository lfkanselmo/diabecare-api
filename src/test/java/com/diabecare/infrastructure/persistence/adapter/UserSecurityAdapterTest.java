package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadUserSecurityPort;
import com.diabecare.infrastructure.persistence.entity.UserEntity;
import com.diabecare.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSecurityAdapter")
class UserSecurityAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    private UserSecurityAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserSecurityAdapter(userJpaRepository);
    }

    @Nested
    @DisplayName("findSecurityDataByEmail")
    class FindSecurityDataByEmail {

        @Test
        @DisplayName("construye UserSecurityData con los 4 campos correctos cuando el usuario existe")
        void buildsUserSecurityDataWithCorrectFieldsWhenExists() {
            UserEntity entity = UserEntity.builder()
                    .id(UUID.randomUUID())
                    .email("ana@example.com")
                    .password("hashed-password")
                    .role("PATIENT")
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userJpaRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(entity));

            Optional<LoadUserSecurityPort.UserSecurityData> result =
                    adapter.findSecurityDataByEmail("ana@example.com");

            assertThat(result).isPresent();
            assertThat(result.get().email()).isEqualTo("ana@example.com");
            assertThat(result.get().password()).isEqualTo("hashed-password");
            assertThat(result.get().enabled()).isTrue();
            assertThat(result.get().role()).isEqualTo("PATIENT");
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el correo no existe")
        void returnsEmptyWhenEmailDoesNotExist() {
            when(userJpaRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

            Optional<LoadUserSecurityPort.UserSecurityData> result =
                    adapter.findSecurityDataByEmail("noexiste@example.com");

            assertThat(result).isEmpty();
        }
    }
}