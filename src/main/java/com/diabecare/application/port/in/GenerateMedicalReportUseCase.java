package com.diabecare.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public interface GenerateMedicalReportUseCase {

    record Command(
            UUID patientId,
            LocalDate from,
            LocalDate to
    ) {}

    byte[] generate(Command command);
}