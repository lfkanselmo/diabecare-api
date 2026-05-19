package com.diabecare.application.port.out;

import com.diabecare.domain.model.Medication;

public interface SaveMedicationPort {
    Medication save(Medication medication);
}