package com.diabecare.application.port.in;

import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterExerciseUseCase {

    record Command(
            UUID patientId,
            ExerciseType exerciseType,
            ExerciseIntensity intensity,
            Integer durationMinutes,
            String notes,
            LocalDateTime performedAt
    ) {}

    ExerciseLog execute(Command command);
}