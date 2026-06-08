package com.diabecare.application.port.out;

import com.diabecare.domain.model.ExerciseLog;

public interface SaveExerciseLogPort {
    ExerciseLog save(ExerciseLog exerciseLog);
}