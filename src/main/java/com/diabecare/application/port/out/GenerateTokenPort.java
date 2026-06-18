package com.diabecare.application.port.out;

import java.util.UUID;

public interface GenerateTokenPort {
    String generateToken(String email, UUID userId);
    long   getExpiresIn();
}