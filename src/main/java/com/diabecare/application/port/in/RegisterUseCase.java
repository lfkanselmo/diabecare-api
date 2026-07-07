package com.diabecare.application.port.in;

import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RegisterUseCase {

    record Command(
            String       email,
            String       password,
            String       fullName,
            LocalDate    dateOfBirth,
            DiabetesType diabetesType,
            LocalDate    diagnosisDate,
            BigDecimal   heightCm,
            BiologicalSex biologicalSex,
            String       deviceLabel,
            String       clientIp
    ) {}

    record Result(
            String token,
            long   expiresIn,
            String refreshToken,
            long   refreshExpiresIn,
            String patientId,
            String userId
    ) {}

    Result execute(Command command);
}