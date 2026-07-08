package com.diabecare.application.port.out;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.domain.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SaveUserPort {
    UserRecord save(String email, String encodedPassword, String role,
                     LocalDateTime termsAcceptedAt, String termsVersion);
    boolean    existsByEmail(String email);
    void       suspend(User user);
    void       delete(User user);
    void       updateRole(UUID userId, String role);
}