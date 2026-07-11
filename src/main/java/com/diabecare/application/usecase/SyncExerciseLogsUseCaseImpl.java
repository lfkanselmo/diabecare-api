package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SyncExerciseLogsUseCase;
import com.diabecare.application.port.out.LoadExerciseLogPort;
import com.diabecare.domain.model.ExerciseLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SyncExerciseLogsUseCaseImpl implements SyncExerciseLogsUseCase {

    private static final LocalDateTime BEGINNING_OF_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final LoadExerciseLogPort loadExerciseLogPort;

    @Override
    public List<ExerciseLog> execute(UUID patientId, LocalDateTime since) {
        return loadExerciseLogPort.findByPatientIdUpdatedAfter(
                patientId, since != null ? since : BEGINNING_OF_TIME);
    }
}
