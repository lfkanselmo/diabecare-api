package com.diabecare.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateUserAdapter")
class AuthenticateUserAdapterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthenticateUserAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuthenticateUserAdapter(authenticationManager);
    }

    @Nested
    @DisplayName("authenticate")
    class Authenticate {

        @Test
        @DisplayName("delega la autenticación al AuthenticationManager con el email y password correctos")
        void delegatesAuthenticationWithCorrectEmailAndPassword() {
            adapter.authenticate("ana@example.com", "password123");

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(captor.capture());

            assertThat(captor.getValue().getPrincipal()).isEqualTo("ana@example.com");
            assertThat(captor.getValue().getCredentials()).isEqualTo("password123");
        }

        @Test
        @DisplayName("propaga la excepción cuando las credenciales son inválidas")
        void propagatesExceptionWhenCredentialsInvalid() {
            doThrow(new BadCredentialsException("credenciales inválidas"))
                    .when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> adapter.authenticate("ana@example.com", "wrong"))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }
}