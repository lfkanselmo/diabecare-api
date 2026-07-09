package com.diabecare.infrastructure.security;

public final class PublicEndpoints {

    private PublicEndpoints() {}

    // /api/v1/metadata/** es pública porque solo expone catálogos estáticos (tipos de
    // diabetes, unidades, niveles de actividad, etc.) sin ningún dato de paciente — y la
    // pantalla de registro (sin sesión todavía) necesita poblar sus selects con estos
    // catálogos antes de que exista un usuario autenticado.
    //
    // /api/v1/glucose/import es pública a nivel de filtro JWT por la misma razón que
    // /api/v1/auth/**: un bridge externo (CGM, Nightscout) no hace login interactivo.
    // Se autentica con una API key de dispositivo validada manualmente dentro del
    // caso de uso — ver DeviceGlucoseImportController.
    public static final String[] PATTERNS = {
            "/api/v1/auth/**",
            "/api/v1/metadata/**",
            "/api/v1/glucose/import",
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
                || path.equals("/api/v1/glucose/import")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.equals("/actuator/health");
    }
}