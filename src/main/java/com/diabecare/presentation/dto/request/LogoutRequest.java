package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LogoutRequest(
        @NotNull
        UUID userId
) {}