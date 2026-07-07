package com.diabecare.presentation.dto.response;

import java.util.UUID;

public record RedeemCaregiverInviteResponse(UUID patientId, String patientFullName) {}
