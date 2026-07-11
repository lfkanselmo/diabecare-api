package com.diabecare.application.port.out;

import com.diabecare.domain.model.Medication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadMedicationPort {
    Optional<Medication> findById(UUID medicationId);
    List<Medication> findActiveByPatientId(UUID patientId);
    List<Medication> findAllByPatientId(UUID patientId);
    // Cruza todos los pacientes: usado por el job de recordatorios, no por endpoints
    // por-paciente.
    List<Medication> findAllActive();

    // Cursor de sincronización incremental para el móvil offline-first — ver
    // MedicationJpaRepository.findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc.
    List<Medication> findByPatientIdUpdatedAfter(UUID patientId, LocalDateTime since);
}