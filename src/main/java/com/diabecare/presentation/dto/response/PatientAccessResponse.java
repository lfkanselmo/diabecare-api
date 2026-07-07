package com.diabecare.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PatientAccessResponse(
        UUID patientId,
        String patientFullName,
        LocalDateTime linkedAt
) {}
