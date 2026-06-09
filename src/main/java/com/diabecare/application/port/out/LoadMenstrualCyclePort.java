package com.diabecare.application.port.out;

import com.diabecare.domain.model.MenstrualCycle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadMenstrualCyclePort {
    List<MenstrualCycle> findByPatientId(UUID patientId);
    Optional<MenstrualCycle> findLatestByPatientId(UUID patientId);
}