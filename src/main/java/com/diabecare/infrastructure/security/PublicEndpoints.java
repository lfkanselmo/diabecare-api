package com.diabecare.infrastructure.security;

public final class PublicEndpoints {

    private PublicEndpoints() {}

    public static final String[] PATTERNS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/api-docs/**",
            "/actuator/health"
    };

    public static boolean matches(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.equals("/actuator/health");
    }
}