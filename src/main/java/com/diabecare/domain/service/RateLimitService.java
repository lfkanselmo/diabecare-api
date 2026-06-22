package com.diabecare.domain.service;

import com.diabecare.application.port.out.RateLimitPort;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitPort    rateLimitPort;
    private final SystemConfigPort systemConfig;

    public void checkGlucoseLimit(UUID patientId) {
        checkLimit(patientId, "GLUCOSE",
                systemConfig.getInt("rate_limit.glucose_per_hour"));
    }

    public void checkMealLimit(UUID patientId) {
        checkLimit(patientId, "MEAL",
                systemConfig.getInt("rate_limit.meal_per_hour"));
    }

    public void checkExerciseLimit(UUID patientId) {
        checkLimit(patientId, "EXERCISE",
                systemConfig.getInt("rate_limit.exercise_per_hour"));
    }

    private void checkLimit(UUID patientId, String operation, int limit) {
        boolean allowed = rateLimitPort.tryConsume(operation, patientId, limit);

        if (!allowed) {
            throw new RateLimitExceededException(
                    "Límite de registros excedido para " + operation +
                            ". Intenta de nuevo más tarde.");
        }
    }
}