package com.diabecare.application.port.out;

import com.diabecare.domain.model.ExerciseLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LoadExerciseLogPort {
    List<ExerciseLog> findByPatientIdAndDateRange(UUID patientId,
                                                  LocalDateTime from,
                                                  LocalDateTime to);
}