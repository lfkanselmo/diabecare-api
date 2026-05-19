package com.diabecare.application.port.in;

import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface RegisterPatientUseCase {

    record Command(
            UUID userId,
            String fullName,
            LocalDate dateOfBirth,
            DiabetesType diabetesType,
            LocalDate diagnosisDate,
            BigDecimal heightCm
    ) {}

    Patient execute(Command command);
}