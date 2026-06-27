package com.diabecare.application.port.in;

import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CycleSymptom;
import com.diabecare.domain.model.FlowIntensity;
import com.diabecare.domain.model.SymptomSeverity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RegisterCycleDayEntryUseCase {

    record SymptomInput(CycleSymptom symptom, SymptomSeverity severity) {}

    record Command(
            UUID patientId,
            LocalDate entryDate,
            FlowIntensity flowIntensity,
            String notes,
            List<SymptomInput> symptoms
    ) {}

    CycleDayEntry execute(Command command);
}