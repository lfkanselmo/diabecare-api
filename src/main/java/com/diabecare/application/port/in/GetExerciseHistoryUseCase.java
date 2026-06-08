package com.diabecare.application.port.in;

import com.diabecare.domain.model.ExerciseLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GetExerciseHistoryUseCase {
    List<ExerciseLog> getHistory(UUID patientId, LocalDateTime from, LocalDateTime to);
}