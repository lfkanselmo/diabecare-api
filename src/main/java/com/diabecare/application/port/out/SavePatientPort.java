package com.diabecare.application.port.out;

import com.diabecare.domain.model.Patient;

public interface SavePatientPort {
    Patient save(Patient patient);
}