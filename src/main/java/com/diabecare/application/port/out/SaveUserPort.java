package com.diabecare.application.port.out;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.domain.model.User;

public interface SaveUserPort {
    UserRecord save(String email, String encodedPassword, String role);
    boolean    existsByEmail(String email);
    void       suspend(User user);
    void       delete(User user);
}