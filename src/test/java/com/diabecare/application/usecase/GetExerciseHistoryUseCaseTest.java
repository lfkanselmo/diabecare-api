package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadExerciseLogPort;
import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetExerciseHistoryUseCaseImpl")
class GetExerciseHistoryUseCaseTest {

    @Mock
    private LoadExerciseLogPort loadExerciseLogPort;

    @InjectMocks
    private GetExerciseHistoryUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("getHistory")
    class GetHistory {

        @Test
        @DisplayName("retorna el historial de ejercicios del rango indicado")
        void returnsHistoryForGivenRange() {
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    30, null, LocalDateTime.now().minusDays(1), null);

            when(loadExerciseLogPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(List.of(log));

            List<ExerciseLog> result = useCase.getHistory(patientId, from, to);

            assertThat(result).containsExactly(log);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay ejercicios en el rango")
        void returnsEmptyListWhenNoExercises() {
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();

            when(loadExerciseLogPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(List.of());

            List<ExerciseLog> result = useCase.getHistory(patientId, from, to);

            assertThat(result).isEmpty();
        }
    }
}