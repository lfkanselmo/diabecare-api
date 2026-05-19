package com.diabecare.application.dto;

import java.util.UUID;

public record UserRecord(
        UUID id,
        String email,
        String role
) {}