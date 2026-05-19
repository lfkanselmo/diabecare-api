package com.diabecare.application.port.out;

import com.diabecare.domain.model.GlucoseReading;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadGlucoseReadingPort {
    Optional<GlucoseReading> findById(UUID readingId);
    List<GlucoseReading> findByPatientId(UUID patientId);
    List<GlucoseReading> findByPatientIdAndDateRange(UUID patientId,
                                                     LocalDateTime from,
                                                     LocalDateTime to);
    void deleteById(UUID readingId);
}