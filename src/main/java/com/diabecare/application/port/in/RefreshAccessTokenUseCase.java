package com.diabecare.application.port.in;

public interface RefreshAccessTokenUseCase {

    record Command(String refreshToken) {}

    record Result(
            String accessToken,
            long   accessTokenExpiresIn,
            String refreshToken,
            long   refreshTokenExpiresIn
    ) {}

    Result execute(Command command);
}