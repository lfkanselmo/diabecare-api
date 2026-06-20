package com.diabecare.presentation.util;

public final class DeviceLabelResolver {

    private DeviceLabelResolver() {}

    public static String resolve(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Dispositivo desconocido";
        }

        String os = resolveOs(userAgent);
        String browser = resolveBrowser(userAgent);

        return browser + " en " + os;
    }

    private static String resolveOs(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("iphone")) return "iPhone";
        if (ua.contains("ipad")) return "iPad";
        if (ua.contains("android")) return "Android";
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os")) return "macOS";
        if (ua.contains("linux")) return "Linux";
        return "dispositivo desconocido";
    }

    private static String resolveBrowser(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("chrome/") && !ua.contains("edg/")) return "Chrome";
        if (ua.contains("firefox/")) return "Firefox";
        if (ua.contains("safari/") && !ua.contains("chrome/")) return "Safari";
        return "Navegador";
    }
}