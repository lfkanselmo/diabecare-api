package com.diabecare.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PublicEndpoints")
class PublicEndpointsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/metadata/diabetes-types",
            "/api/v1/metadata/activity-levels",
            "/swagger-ui/index.html",
            "/swagger-ui",
            "/v3/api-docs/swagger-config",
            "/v3/api-docs",
            "/api-docs/openapi.json",
            "/actuator/health"
    })
    @DisplayName("matches retorna true para rutas públicas conocidas")
    void matchesReturnsTrueForKnownPublicRoutes(String path) {
        assertThat(PublicEndpoints.matches(path)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/patients/123",
            "/api/v1/glucose",
            "/actuator/info",
            "/actuator/health/extra",
            "/api/v1/auth-fake/login",
            "/api/v1/metadata-fake/diabetes-types"
    })
    @DisplayName("matches retorna false para rutas privadas o que solo coinciden parcialmente")
    void matchesReturnsFalseForPrivateOrPartiallyMatchingRoutes(String path) {
        assertThat(PublicEndpoints.matches(path)).isFalse();
    }

    @Test
    @DisplayName("matches distingue exactamente /actuator/health de otras rutas bajo /actuator")
    void matchesDistinguishesExactActuatorHealthFromOtherActuatorRoutes() {
        assertThat(PublicEndpoints.matches("/actuator/health")).isTrue();
        assertThat(PublicEndpoints.matches("/actuator/health/")).isFalse();
        assertThat(PublicEndpoints.matches("/actuator/healthcheck")).isFalse();
    }
}