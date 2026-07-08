package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String role,
        boolean enabled,
        LocalDateTime suspendedAt,
        LocalDateTime deletedAt,
        LocalDateTime createdAt
) {}
