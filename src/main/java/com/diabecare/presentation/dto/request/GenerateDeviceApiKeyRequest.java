package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateDeviceApiKeyRequest(
        @NotBlank @Size(max = 100)
        String label
) {}
