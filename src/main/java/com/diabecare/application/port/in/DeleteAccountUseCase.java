package com.diabecare.application.port.in;

import java.util.UUID;

public interface DeleteAccountUseCase {
    void execute(UUID userId);
}