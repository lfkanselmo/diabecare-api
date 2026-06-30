package com.diabecare.infrastructure.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RestAuthenticationEntryPoint")
class RestAuthenticationEntryPointTest {

    private RestAuthenticationEntryPoint entryPoint;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        entryPoint = new RestAuthenticationEntryPoint(objectMapper);
    }

    @Nested
    @DisplayName("commence")
    class Commence {

        @Test
        @DisplayName("responde con status 401 y content-type JSON")
        void respondsWithStatus401AndJsonContentType() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/v1/patients/me");
            MockHttpServletResponse response = new MockHttpServletResponse();

            entryPoint.commence(request, response, new BadCredentialsException("no autenticado"));

            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("incluye el código SESSION_EXPIRED, el mensaje y la ruta solicitada en el cuerpo")
        void includesSessionExpiredCodeMessageAndRequestPathInBody() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/v1/patients/me");
            MockHttpServletResponse response = new MockHttpServletResponse();

            entryPoint.commence(request, response, new BadCredentialsException("no autenticado"));

            JsonNode body = objectMapper.readTree(response.getContentAsString());

            assertThat(body.get("status").asInt()).isEqualTo(401);
            assertThat(body.get("code").asText()).isEqualTo("SESSION_EXPIRED");
            assertThat(body.get("path").asText()).isEqualTo("/api/v1/patients/me");
            assertThat(body.get("message").asText()).isNotBlank();
        }
    }
}