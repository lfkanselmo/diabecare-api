package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadExerciseLogPort;
import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncExerciseLogsUseCaseImpl")
class SyncExerciseLogsUseCaseTest {

    @Mock
    private LoadExerciseLogPort loadExerciseLogPort;

    private SyncExerciseLogsUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @Test
    @DisplayName("delega al puerto con el cursor exacto cuando since viene informado")
    void delegatesToPortWithExactCursorWhenSinceIsProvided() {
        useCase = new SyncExerciseLogsUseCaseImpl(loadExerciseLogPort);
        LocalDateTime since = LocalDateTime.of(2026, 1, 1, 0, 0);
        ExerciseLog log = ExerciseLog.create(
                patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE, 30, null,
                LocalDateTime.now().minusMinutes(5), null);
        when(loadExerciseLogPort.findByPatientIdUpdatedAfter(patientId, since)).thenReturn(List.of(log));

        List<ExerciseLog> result = useCase.execute(patientId, since);

        assertThat(result).containsExactly(log);
    }

    @Test
    @DisplayName("usa una época segura en vez de null cuando since está ausente")
    void usesSafeEpochInsteadOfNullWhenSinceIsAbsent() {
        useCase = new SyncExerciseLogsUseCaseImpl(loadExerciseLogPort);
        when(loadExerciseLogPort.findByPatientIdUpdatedAfter(any(), any())).thenReturn(List.of());

        useCase.execute(patientId, null);

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(loadExerciseLogPort).findByPatientIdUpdatedAfter(eq(patientId), sinceCaptor.capture());
        assertThat(sinceCaptor.getValue()).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
    }
}
