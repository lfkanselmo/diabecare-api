package com.diabecare.application.port.in;

import java.util.UUID;

public interface SuspendAccountUseCase {
    void execute(UUID userId);
}