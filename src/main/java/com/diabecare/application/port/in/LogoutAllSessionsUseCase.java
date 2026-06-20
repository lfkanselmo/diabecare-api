package com.diabecare.application.port.in;

import java.util.UUID;

public interface LogoutAllSessionsUseCase {

    record Command(UUID userId) {}

    void execute(Command command);
}