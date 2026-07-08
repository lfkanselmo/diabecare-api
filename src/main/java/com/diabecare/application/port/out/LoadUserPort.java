package com.diabecare.application.port.out;

import com.diabecare.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadUserPort {
    Optional<UUID> findUserIdByEmail(String email);
    Optional<User> findById(UUID userId);
    List<UUID> findIdsDeletedBefore(LocalDateTime cutoff);
    Page<User> findAll(Pageable pageable);
}