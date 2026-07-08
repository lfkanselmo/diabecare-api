package com.diabecare.application.port.in;

import com.diabecare.domain.model.ExerciseLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GetExerciseHistoryUseCase {
    Page<ExerciseLog> getHistory(UUID patientId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}