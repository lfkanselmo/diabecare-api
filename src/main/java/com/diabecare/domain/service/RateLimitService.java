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
        checkLimit(patientId.toString(), "GLUCOSE",
                systemConfig.getInt("rate_limit.glucose_per_hour"));
    }

    public void checkMealLimit(UUID patientId) {
        checkLimit(patientId.toString(), "MEAL",
                systemConfig.getInt("rate_limit.meal_per_hour"));
    }

    public void checkExerciseLimit(UUID patientId) {
        checkLimit(patientId.toString(), "EXERCISE",
                systemConfig.getInt("rate_limit.exercise_per_hour"));
    }

    /**
     * Clave por IP, no por usuario: antes de autenticarse no existe un patientId,
     * y es justamente el intento de fuerza bruta lo que se quiere frenar.
     */
    public void checkLoginLimit(String clientIp) {
        checkLimit(clientIp, "LOGIN",
                systemConfig.getInt("rate_limit.login_per_hour"));
    }

    public void checkRegisterLimit(String clientIp) {
        checkLimit(clientIp, "REGISTER",
                systemConfig.getInt("rate_limit.register_per_hour"));
    }

    public void checkForgotPasswordLimit(String clientIp) {
        checkLimit(clientIp, "FORGOT_PASSWORD",
                systemConfig.getInt("rate_limit.forgot_password_per_hour"));
    }

    private void checkLimit(String subjectKey, String operation, int limit) {
        boolean allowed = rateLimitPort.tryConsume(operation, subjectKey, limit);

        if (!allowed) {
            throw new RateLimitExceededException(
                    "Límite de registros excedido para " + operation +
                            ". Intenta de nuevo más tarde.");
        }
    }
}