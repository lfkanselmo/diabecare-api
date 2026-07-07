package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CaregiverLinkResponse(
        UUID linkId,
        UUID caregiverUserId,
        String caregiverName,
        String caregiverEmail,
        LocalDateTime linkedAt
) {}
