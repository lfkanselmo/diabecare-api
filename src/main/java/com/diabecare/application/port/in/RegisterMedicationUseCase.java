package com.diabecare.application.port.in;

import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface RegisterMedicationUseCase {

    record Command(
            UUID patientId,
            String name,
            MedicationType type,
            BigDecimal dose,
            DoseUnit doseUnit,
            MedicationFrequency frequency,
            LocalDate startDate,
            String notes
    ) {}

    Medication execute(Command command);
}