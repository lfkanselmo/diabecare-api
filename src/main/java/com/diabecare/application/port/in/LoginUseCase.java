package com.diabecare.application.port.in;

public interface LoginUseCase {

    record Command(
            String email,
            String password,
            String deviceLabel
    ) {}

    record Result(
            String token,
            long   expiresIn,
            String refreshToken,
            long   refreshExpiresIn,
            String patientId,
            String userId
    ) {}

    Result execute(Command command);
}