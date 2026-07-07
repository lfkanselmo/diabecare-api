package com.diabecare.application.port.in;

import com.diabecare.application.dto.UserRecord;

public interface RegisterUserUseCase {

    record Command(
            String email,
            String password,
            String termsVersion
    ) {}

    UserRecord execute(Command command);
}