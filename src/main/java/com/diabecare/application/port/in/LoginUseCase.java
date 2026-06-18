package com.diabecare.application.port.in;

import com.diabecare.presentation.dto.response.AuthResponse;

public interface LoginUseCase {

    record Command(
            String email,
            String password
    ) {}

    record Result(
            String token,
            long   expiresIn,
            String patientId,
            String userId
    ) {}

    Result execute(Command command);
}