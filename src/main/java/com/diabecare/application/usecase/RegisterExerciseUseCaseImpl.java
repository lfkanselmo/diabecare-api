package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterExerciseUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveExerciseLogPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterExerciseUseCaseImpl implements RegisterExerciseUseCase {

    private final SaveExerciseLogPort saveExerciseLogPort;
    private final LoadPatientPort     loadPatientPort;
    private final RateLimitService    rateLimitService;

    @Override
    public ExerciseLog execute(Command command) {
        rateLimitService.checkExerciseLimit(command.patientId());

        loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        ExerciseLog log = command.clientExerciseId() != null
                ? ExerciseLog.createWithId(
                        command.clientExerciseId(),
                        command.patientId(),
                        command.exerciseType(),
                        command.intensity(),
                        command.durationMinutes(),
                        command.notes(),
                        command.performedAt(),
                        command.caloriesBurnedOverride())
                : ExerciseLog.create(
                        command.patientId(),
                        command.exerciseType(),
                        command.intensity(),
                        command.durationMinutes(),
                        command.notes(),
                        command.performedAt(),
                        command.caloriesBurnedOverride());
        return saveExerciseLogPort.save(log);
    }
}