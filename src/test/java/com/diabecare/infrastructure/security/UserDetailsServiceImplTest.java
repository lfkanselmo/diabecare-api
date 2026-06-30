package com.diabecare.infrastructure.security;

import com.diabecare.application.port.out.LoadUserSecurityPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private LoadUserSecurityPort loadUserSecurityPort;

    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserDetailsServiceImpl(loadUserSecurityPort);
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("construye UserDetails con el rol prefijado correctamente con ROLE_")
        void buildsUserDetailsWithRolePrefixedCorrectly() {
            var securityData = new LoadUserSecurityPort.UserSecurityData(
                    "ana@example.com", "hashed-password", true, "PATIENT");
            when(loadUserSecurityPort.findSecurityDataByEmail("ana@example.com"))
                    .thenReturn(Optional.of(securityData));

            UserDetails result = service.loadUserByUsername("ana@example.com");

            assertThat(result.getUsername()).isEqualTo("ana@example.com");
            assertThat(result.getPassword()).isEqualTo("hashed-password");
            assertThat(result.isEnabled()).isTrue();
            assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_PATIENT");
        }

        @Test
        @DisplayName("construye UserDetails deshabilitado cuando el usuario está deshabilitado")
        void buildsDisabledUserDetailsWhenUserIsDisabled() {
            var securityData = new LoadUserSecurityPort.UserSecurityData(
                    "ana@example.com", "hashed-password", false, "PATIENT");
            when(loadUserSecurityPort.findSecurityDataByEmail("ana@example.com"))
                    .thenReturn(Optional.of(securityData));

            UserDetails result = service.loadUserByUsername("ana@example.com");

            assertThat(result.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("lanza UsernameNotFoundException cuando el correo no existe")
        void throwsUsernameNotFoundExceptionWhenEmailDoesNotExist() {
            when(loadUserSecurityPort.findSecurityDataByEmail("noexiste@example.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.loadUserByUsername("noexiste@example.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("noexiste@example.com");
        }
    }
}