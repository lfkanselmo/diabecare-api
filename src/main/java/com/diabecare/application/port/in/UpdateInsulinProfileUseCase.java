package com.diabecare.application.port.in;

import com.diabecare.domain.model.Patient;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateInsulinProfileUseCase {

    record Command(
            UUID patientId,
            BigDecimal sensitivityFactor,
            BigDecimal carbRatio,
            BigDecimal targetGlucose
    ) {}

    Patient execute(Command command);
}