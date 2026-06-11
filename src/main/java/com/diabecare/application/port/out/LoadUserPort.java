package com.diabecare.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface LoadUserPort {
    Optional<UUID> findUserIdByEmail(String email);
}