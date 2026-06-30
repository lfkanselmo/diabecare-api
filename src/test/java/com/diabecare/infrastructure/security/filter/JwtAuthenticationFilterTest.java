package com.diabecare.infrastructure.security.filter;

import com.diabecare.infrastructure.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("doFilterInternal")
    class DoFilterInternal {

        @Test
        @DisplayName("autentica al usuario cuando el header Bearer contiene un token válido")
        void authenticatesUserWhenBearerHeaderContainsValidToken() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid-jwt-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            UserDetails userDetails = new User("ana@example.com", "hash", true, true, true, true,
                    Collections.singletonList(() -> "ROLE_PATIENT"));

            when(jwtService.extractUsername("valid-jwt-token")).thenReturn("ana@example.com");
            when(userDetailsService.loadUserByUsername("ana@example.com")).thenReturn(userDetails);
            when(jwtService.isTokenValid("valid-jwt-token", userDetails)).thenReturn(true);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo("ana@example.com");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("continúa la cadena sin autenticar cuando no hay header Authorization")
        void continuesChainWithoutAuthenticatingWhenNoAuthorizationHeader() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("continúa la cadena sin autenticar cuando el header no tiene el prefijo Bearer")
        void continuesChainWithoutAuthenticatingWhenHeaderLacksBearerPrefix() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("no autentica y continúa la cadena cuando el token es inválido")
        void doesNotAuthenticateAndContinuesChainWhenTokenInvalid() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            UserDetails userDetails = new User("ana@example.com", "hash", true, true, true, true,
                    Collections.singletonList(() -> "ROLE_PATIENT"));

            when(jwtService.extractUsername("invalid-token")).thenReturn("ana@example.com");
            when(userDetailsService.loadUserByUsername("ana@example.com")).thenReturn(userDetails);
            when(jwtService.isTokenValid("invalid-token", userDetails)).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("no falla y continúa la cadena cuando el servicio JWT lanza una excepción")
        void doesNotFailAndContinuesChainWhenJwtServiceThrows() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer malformed-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(jwtService.extractUsername("malformed-token"))
                    .thenThrow(new RuntimeException("token mal formado"));

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("no sobreescribe una autenticación ya presente en el contexto de seguridad")
        void doesNotOverwriteExistingAuthenticationInContext() throws Exception {
            var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "otro@example.com", null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid-jwt-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(jwtService.extractUsername("valid-jwt-token")).thenReturn("ana@example.com");

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo("otro@example.com");
            verifyNoInteractions(userDetailsService);
        }
    }

    @Nested
    @DisplayName("shouldNotFilter")
    class ShouldNotFilter {

        @Test
        @DisplayName("retorna true para rutas públicas")
        void returnsTrueForPublicRoutes() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setServletPath("/api/v1/auth/login");

            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("retorna false para rutas privadas")
        void returnsFalseForPrivateRoutes() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setServletPath("/api/v1/patients/123");

            assertThat(filter.shouldNotFilter(request)).isFalse();
        }
    }
}