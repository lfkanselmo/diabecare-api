package com.diabecare.presentation.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        PatientResponse patient
) {
    public static AuthResponse of(String token, long expiresIn, PatientResponse patient) {
        return new AuthResponse(token, "Bearer", expiresIn, patient);
    }
}