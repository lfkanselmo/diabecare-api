package com.diabecare.infrastructure.security;

public final class PublicEndpoints {

    private PublicEndpoints() {}

    // /api/v1/metadata/** es pública porque solo expone catálogos estáticos (tipos de
    // diabetes, unidades, niveles de actividad, etc.) sin ningún dato de paciente — y la
    // pantalla de registro (sin sesión todavía) necesita poblar sus selects con estos
    // catálogos antes de que exista un usuario autenticado.
    public static final String[] PATTERNS = {
            "/api/v1/auth/**",
            "/api/v1/metadata/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/api-docs/**",
            "/actuator/health"
    };

    public static boolean matches(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/metadata/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.equals("/actuator/health");
    }
}