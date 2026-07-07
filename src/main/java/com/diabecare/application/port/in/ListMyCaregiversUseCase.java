package com.diabecare.application.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ListMyCaregiversUseCase {

    record CaregiverView(
            UUID linkId,
            UUID caregiverUserId,
            String caregiverName,
            String caregiverEmail,
            LocalDateTime linkedAt
    ) {}

    List<CaregiverView> execute(UUID patientId);
}
