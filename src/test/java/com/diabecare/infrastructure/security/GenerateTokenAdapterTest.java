package com.diabecare.infrastructure.security;

import com.diabecare.infrastructure.config.JwtProperties;
import com.diabecare.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateTokenAdapter")
class GenerateTokenAdapterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtProperties jwtProperties;

    private GenerateTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GenerateTokenAdapter(jwtService, userDetailsService, jwtProperties);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("carga el UserDetails por email y genera el token a partir de él")
        void loadsUserDetailsByEmailAndGeneratesTokenFromIt() {
            UUID userId = UUID.randomUUID();
            UserDetails userDetails = new User("ana@example.com", "hash", true, true, true, true,
                    Collections.singletonList(() -> "ROLE_PATIENT"));

            when(userDetailsService.loadUserByUsername("ana@example.com")).thenReturn(userDetails);
            when(jwtService.generateAccessToken(userDetails, userId)).thenReturn("generated-token");

            String result = adapter.generateToken("ana@example.com", userId);

            assertThat(result).isEqualTo("generated-token");
            verify(jwtService).generateAccessToken(userDetails, userId);
        }
    }

    @Nested
    @DisplayName("getExpiresIn")
    class GetExpiresIn {

        @Test
        @DisplayName("retorna el tiempo de expiración configurado en JwtProperties")
        void returnsConfiguredExpiryTime() {
            when(jwtProperties.getAccessTokenExpiryMs()).thenReturn(900_000L);

            long result = adapter.getExpiresIn();

            assertThat(result).isEqualTo(900_000L);
        }
    }
}