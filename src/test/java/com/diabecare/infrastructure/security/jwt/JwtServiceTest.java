package com.diabecare.infrastructure.security.jwt;

import com.diabecare.infrastructure.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String TEST_SECRET =
            "57VZklHtQx95rxOosCSq60ifjnc45vz2MOYYId88_yn3cXkhpEaj8BxDskpUc0TS";

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(TEST_SECRET);
        jwtProperties.setAccessTokenExpiryMs(900_000L);
        jwtProperties.setRefreshTokenExpiryMs(604_800_000L);
        jwtService = new JwtService(jwtProperties);
    }

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateAccessToken {

        @Test
        @DisplayName("genera un token cuyo subject coincide con el username del usuario")
        void generatesTokenWithSubjectMatchingUsername() {
            UserDetails userDetails = validUserDetails("ana@example.com");

            String token = jwtService.generateAccessToken(userDetails, UUID.randomUUID());

            assertThat(jwtService.extractUsername(token)).isEqualTo("ana@example.com");
        }

        @Test
        @DisplayName("genera tokens distintos para llamadas distintas")
        void generatesDifferentTokensForDifferentCalls() throws InterruptedException {
            UserDetails userDetails = validUserDetails("ana@example.com");

            String token1 = jwtService.generateAccessToken(userDetails, UUID.randomUUID());
            Thread.sleep(10);
            String token2 = jwtService.generateAccessToken(userDetails, UUID.randomUUID());

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("retorna true para un token recién generado del mismo usuario")
        void returnsTrueForFreshlyGeneratedTokenOfSameUser() {
            UserDetails userDetails = validUserDetails("ana@example.com");
            String token = jwtService.generateAccessToken(userDetails, UUID.randomUUID());

            assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        }

        @Test
        @DisplayName("retorna false cuando el token pertenece a un usuario distinto")
        void returnsFalseWhenTokenBelongsToDifferentUser() {
            UserDetails originalUser = validUserDetails("ana@example.com");
            UserDetails otherUser = validUserDetails("carlos@example.com");
            String token = jwtService.generateAccessToken(originalUser, UUID.randomUUID());

            assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
        }

        @Test
        @DisplayName("retorna false cuando el token está mal formado")
        void returnsFalseWhenTokenIsMalformed() {
            UserDetails userDetails = validUserDetails("ana@example.com");

            assertThat(jwtService.isTokenValid("token-invalido-no-jwt", userDetails)).isFalse();
        }

        @Test
        @DisplayName("retorna false cuando el token fue firmado con una clave secreta distinta")
        void returnsFalseWhenTokenSignedWithDifferentSecret() {
            JwtProperties otherProperties = new JwtProperties();
            otherProperties.setSecretKey("otraClaveSecretaCompletamenteDistintaDeMinimo32Bytes123456");
            otherProperties.setAccessTokenExpiryMs(900_000L);
            JwtService otherService = new JwtService(otherProperties);

            UserDetails userDetails = validUserDetails("ana@example.com");
            String tokenFromOtherService = otherService.generateAccessToken(userDetails, UUID.randomUUID());

            assertThat(jwtService.isTokenValid(tokenFromOtherService, userDetails)).isFalse();
        }

        @Test
        @DisplayName("retorna false cuando el token ya expiró")
        void returnsFalseWhenTokenAlreadyExpired() {
            jwtProperties.setAccessTokenExpiryMs(-1000L);
            JwtService expiredService = new JwtService(jwtProperties);
            UserDetails userDetails = validUserDetails("ana@example.com");

            String expiredToken = expiredService.generateAccessToken(userDetails, UUID.randomUUID());

            assertThat(jwtService.isTokenValid(expiredToken, userDetails)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractExpiration")
    class ExtractExpiration {

        @Test
        @DisplayName("extrae una fecha de expiración futura para un token recién generado")
        void extractsFutureExpirationForFreshlyGeneratedToken() {
            UserDetails userDetails = validUserDetails("ana@example.com");
            String token = jwtService.generateAccessToken(userDetails, UUID.randomUUID());

            assertThat(jwtService.extractExpiration(token)).isAfter(new java.util.Date());
        }
    }


    private UserDetails validUserDetails(String email) {
        return new User(email, "irrelevant-password", true, true, true, true,
                Collections.singletonList(() -> "ROLE_PATIENT"));
    }
}
