package com.diabecare.application.port.out;

import com.diabecare.domain.model.GlucoseReminder;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadGlucoseReminderPort {
    Optional<GlucoseReminder> findById(UUID reminderId);
    List<GlucoseReminder> findByPatientId(UUID patientId);
    // Cruza todos los pacientes: usado por el job de recordatorios.
    List<GlucoseReminder> findAllEnabledAtTime(LocalTime time);
}
