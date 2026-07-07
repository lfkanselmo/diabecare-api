package com.diabecare.application.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ListPatientsICareForUseCase {

    record PatientAccessView(
            UUID patientId,
            String patientFullName,
            LocalDateTime linkedAt
    ) {}

    List<PatientAccessView> execute(UUID caregiverUserId);
}
