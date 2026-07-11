package com.diabecare.infrastructure.security;

public final class PublicEndpoints {

    private PublicEndpoints() {}

    // /api/v1/metadata/** es pública porque solo expone catálogos estáticos (tipos de
    // diabetes, unidades, niveles de actividad, etc.) sin ningún dato de paciente — y la
    // pantalla de registro (sin sesión todavía) necesita poblar sus selects con estos
    // catálogos antes de que exista un usuario autenticado.
    //
    // /api/v1/glucose/import es pública a nivel de filtro JWT por la misma razón que
    // los endpoints de /api/v1/auth listados abajo: un bridge externo (CGM, Nightscout)
    // no hace login interactivo. Se autentica con una API key de dispositivo validada
    // manualmente dentro del caso de uso — ver DeviceGlucoseImportController.
    //
    // Solo estos 6 endpoints de AuthController son públicos — a propósito NO se usa
    // un wildcard "/api/v1/auth/**": /auth/sessions/{userId} y /auth/logout-all
    // requieren JWT (usan Authentication en el controller), y un wildcard los dejaba
    // pasar sin pasar por el filtro, causando un NPE en cuanto alguien los llamaba
    // (Authentication llegaba null al controller).
    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    };

    public static final String[] PATTERNS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
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
        for (String publicAuthEndpoint : PUBLIC_AUTH_ENDPOINTS) {
            if (path.equals(publicAuthEndpoint)) {
                return true;
            }
        }
        return path.startsWith("/api/v1/metadata/")
                || path.equals("/api/v1/glucose/import")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.equals("/actuator/health");
    }
}