package com.diabecare.presentation.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        PatientResponse patient
) {
    public static AuthResponse of(String token, long expiresIn, String refreshToken,
                                  long refreshExpiresIn, PatientResponse patient) {
        return new AuthResponse(token, "Bearer", expiresIn, refreshToken, refreshExpiresIn, patient);
    }
}