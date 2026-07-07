package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;

public record CaregiverInviteResponse(String code, LocalDateTime expiresAt) {}
