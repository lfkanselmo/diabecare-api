package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeUserRoleRequest(
        @NotBlank(message = "El rol es obligatorio")
        String role
) {}
