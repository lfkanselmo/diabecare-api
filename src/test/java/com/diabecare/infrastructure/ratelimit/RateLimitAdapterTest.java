package com.diabecare.infrastructure.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RateLimitAdapter")
class RateLimitAdapterTest {

    private final RateLimitAdapter adapter = new RateLimitAdapter();
    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("tryConsume")
    class TryConsume {

        @Test
        @DisplayName("permite consumir hasta el límite configurado")
        void allowsConsumingUpToTheConfiguredLimit() {
            for (int i = 0; i < 5; i++) {
                assertThat(adapter.tryConsume("GLUCOSE", patientId, 5)).isTrue();
            }
        }

        @Test
        @DisplayName("rechaza el intento que excede el límite configurado")
        void rejectsAttemptExceedingTheConfiguredLimit() {
            for (int i = 0; i < 5; i++) {
                adapter.tryConsume("GLUCOSE", patientId, 5);
            }

            boolean sixthAttempt = adapter.tryConsume("GLUCOSE", patientId, 5);

            assertThat(sixthAttempt).isFalse();
        }

        @Test
        @DisplayName("mantiene límites independientes para distintos pacientes")
        void keepsIndependentLimitsForDifferentPatients() {
            UUID otherPatientId = UUID.randomUUID();

            for (int i = 0; i < 3; i++) {
                adapter.tryConsume("GLUCOSE", patientId, 3);
            }

            boolean otherPatientAttempt = adapter.tryConsume("GLUCOSE", otherPatientId, 3);

            assertThat(otherPatientAttempt).isTrue();
        }

        @Test
        @DisplayName("mantiene límites independientes para distintas operaciones del mismo paciente")
        void keepsIndependentLimitsForDifferentOperations() {
            for (int i = 0; i < 3; i++) {
                adapter.tryConsume("GLUCOSE", patientId, 3);
            }

            boolean differentOperationAttempt = adapter.tryConsume("MEAL", patientId, 3);

            assertThat(differentOperationAttempt).isTrue();
        }
    }
}