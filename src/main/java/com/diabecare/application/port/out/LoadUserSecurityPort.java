package com.diabecare.application.port.out;

import java.util.Optional;

public interface LoadUserSecurityPort {

    record UserSecurityData(
            String email,
            String password,
            boolean enabled,
            String role
    ) {}

    Optional<UserSecurityData> findSecurityDataByEmail(String email);
}