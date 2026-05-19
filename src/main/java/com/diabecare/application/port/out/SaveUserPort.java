package com.diabecare.application.port.out;

import com.diabecare.application.dto.UserRecord;

public interface SaveUserPort {
    UserRecord save(String email, String encodedPassword, String role);
    boolean existsByEmail(String email);
}