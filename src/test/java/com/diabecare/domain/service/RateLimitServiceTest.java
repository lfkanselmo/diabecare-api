package com.diabecare.domain.service;

import com.diabecare.application.port.out.RateLimitPort;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService")
class RateLimitServiceTest {

    @Mock
    private RateLimitPort rateLimitPort;

    @Mock
    private SystemConfigPort systemConfig;

    private RateLimitService service;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new RateLimitService(rateLimitPort, systemConfig);
    }

    @Nested
    @DisplayName("checkGlucoseLimit")
    class CheckGlucoseLimit {

        @Test
        @DisplayName("no lanza excepción cuando el límite no se ha excedido")
        void doesNotThrowWhenWithinLimit() {
            when(systemConfig.getInt("rate_limit.glucose_per_hour")).thenReturn(20);
            when(rateLimitPort.tryConsume("GLUCOSE", patientId, 20)).thenReturn(true);

            assertThatCode(() -> service.checkGlucoseLimit(patientId)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("lanza RateLimitExceededException cuando el límite se excede")
        void throwsWhenLimitExceeded() {
            when(systemConfig.getInt("rate_limit.glucose_per_hour")).thenReturn(20);
            when(rateLimitPort.tryConsume("GLUCOSE", patientId, 20)).thenReturn(false);

            assertThatThrownBy(() -> service.checkGlucoseLimit(patientId))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("GLUCOSE");
        }

        @Test
        @DisplayName("usa la clave de configuración correcta")
        void usesCorrectConfigKey() {
            when(systemConfig.getInt("rate_limit.glucose_per_hour")).thenReturn(20);
            when(rateLimitPort.tryConsume(any(), any(), anyInt())).thenReturn(true);

            service.checkGlucoseLimit(patientId);

            verify(systemConfig).getInt("rate_limit.glucose_per_hour");
        }
    }

    @Nested
    @DisplayName("checkMealLimit")
    class CheckMealLimit {

        @Test
        @DisplayName("usa la clave de configuración y operación correctas")
        void usesCorrectKeyAndOperation() {
            when(systemConfig.getInt("rate_limit.meal_per_hour")).thenReturn(15);
            when(rateLimitPort.tryConsume("MEAL", patientId, 15)).thenReturn(true);

            service.checkMealLimit(patientId);

            verify(systemConfig).getInt("rate_limit.meal_per_hour");
            verify(rateLimitPort).tryConsume("MEAL", patientId, 15);
        }

        @Test
        @DisplayName("lanza excepción cuando el límite de comidas se excede")
        void throwsWhenMealLimitExceeded() {
            when(systemConfig.getInt("rate_limit.meal_per_hour")).thenReturn(15);
            when(rateLimitPort.tryConsume("MEAL", patientId, 15)).thenReturn(false);

            assertThatThrownBy(() -> service.checkMealLimit(patientId))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("MEAL");
        }
    }

    @Nested
    @DisplayName("checkExerciseLimit")
    class CheckExerciseLimit {

        @Test
        @DisplayName("usa la clave de configuración y operación correctas")
        void usesCorrectKeyAndOperation() {
            when(systemConfig.getInt("rate_limit.exercise_per_hour")).thenReturn(10);
            when(rateLimitPort.tryConsume("EXERCISE", patientId, 10)).thenReturn(true);

            service.checkExerciseLimit(patientId);

            verify(systemConfig).getInt("rate_limit.exercise_per_hour");
            verify(rateLimitPort).tryConsume("EXERCISE", patientId, 10);
        }

        @Test
        @DisplayName("lanza excepción cuando el límite de ejercicio se excede")
        void throwsWhenExerciseLimitExceeded() {
            when(systemConfig.getInt("rate_limit.exercise_per_hour")).thenReturn(10);
            when(rateLimitPort.tryConsume("EXERCISE", patientId, 10)).thenReturn(false);

            assertThatThrownBy(() -> service.checkExerciseLimit(patientId))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("EXERCISE");
        }
    }
}