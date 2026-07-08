package com.diabecare.application.port.out;

import com.diabecare.domain.model.VitalSign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadVitalSignPort {
    Optional<VitalSign> findById(UUID vitalId);
    Optional<VitalSign> findLatestByPatientId(UUID patientId);
    // Historial completo, usado solo por la exportación de datos (Habeas Data).
    List<VitalSign> findByPatientId(UUID patientId);
    Page<VitalSign> findByPatientId(UUID patientId, Pageable pageable);
}