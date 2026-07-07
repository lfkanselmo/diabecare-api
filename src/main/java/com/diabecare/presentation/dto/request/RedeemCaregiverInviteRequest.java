package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RedeemCaregiverInviteRequest(
        @NotBlank(message = "El código de invitación es obligatorio")
        String code
) {}
