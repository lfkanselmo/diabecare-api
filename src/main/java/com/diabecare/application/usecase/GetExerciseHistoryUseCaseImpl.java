package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetExerciseHistoryUseCase;
import com.diabecare.application.port.out.LoadExerciseLogPort;
import com.diabecare.domain.model.ExerciseLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetExerciseHistoryUseCaseImpl implements GetExerciseHistoryUseCase {

    private final LoadExerciseLogPort loadExerciseLogPort;

    @Override
    public Page<ExerciseLog> getHistory(UUID patientId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return loadExerciseLogPort.findByPatientIdAndDateRange(patientId, from, to, pageable);
    }
}