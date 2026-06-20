package com.diabecare.presentation.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn
) {
    public static RefreshTokenResponse of(String accessToken, long expiresIn,
                                          String refreshToken, long refreshExpiresIn) {
        return new RefreshTokenResponse(accessToken, "Bearer", expiresIn, refreshToken, refreshExpiresIn);
    }
}