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
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RestAccessDeniedHandler")
class RestAccessDeniedHandlerTest {

    private RestAccessDeniedHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        handler = new RestAccessDeniedHandler(objectMapper);
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("responde con status 403 y content-type JSON")
        void respondsWithStatus403AndJsonContentType() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/v1/admin/users");
            MockHttpServletResponse response = new MockHttpServletResponse();

            handler.handle(request, response, new AccessDeniedException("acceso denegado"));

            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
            assertThat(response.getContentType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("incluye el código ACCESS_DENIED, el mensaje y la ruta solicitada en el cuerpo")
        void includesAccessDeniedCodeMessageAndRequestPathInBody() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/v1/admin/users");
            MockHttpServletResponse response = new MockHttpServletResponse();

            handler.handle(request, response, new AccessDeniedException("acceso denegado"));

            JsonNode body = objectMapper.readTree(response.getContentAsString());

            assertThat(body.get("status").asInt()).isEqualTo(403);
            assertThat(body.get("code").asText()).isEqualTo("ACCESS_DENIED");
            assertThat(body.get("path").asText()).isEqualTo("/api/v1/admin/users");
            assertThat(body.get("message").asText()).isNotBlank();
        }
    }
}