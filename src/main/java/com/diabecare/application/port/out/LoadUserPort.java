package com.diabecare.application.port.out;

import com.diabecare.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface LoadUserPort {
    Optional<UUID> findUserIdByEmail(String email);
    Optional<User> findById(UUID userId);
}