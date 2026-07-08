package com.diabecare.presentation.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        PatientResponse patient,
        String role
) {
    public static AuthResponse of(String token, long expiresIn, String refreshToken,
                                  long refreshExpiresIn, PatientResponse patient, String role) {
        return new AuthResponse(token, "Bearer", expiresIn, refreshToken, refreshExpiresIn, patient, role);
    }
}