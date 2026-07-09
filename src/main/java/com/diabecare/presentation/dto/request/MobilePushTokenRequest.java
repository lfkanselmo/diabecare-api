package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MobilePushTokenRequest(
        @NotBlank
        String deviceToken,

        @NotNull
        String platform
) {}
