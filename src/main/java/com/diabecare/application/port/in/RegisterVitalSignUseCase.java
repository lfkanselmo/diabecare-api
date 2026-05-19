package com.diabecare.application.port.in;

import com.diabecare.domain.model.VitalSign;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterVitalSignUseCase {

    record Command(
            UUID patientId,
            BigDecimal weightKg,
            BigDecimal heightCm,
            Integer systolicBp,
            Integer diastolicBp,
            Integer heartRate,
            BigDecimal hba1c,
            LocalDateTime measuredAt,
            String notes
    ) {}

    VitalSign execute(Command command);
}