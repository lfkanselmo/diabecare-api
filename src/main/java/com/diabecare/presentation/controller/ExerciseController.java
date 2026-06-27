package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetExerciseHistoryUseCase;
import com.diabecare.application.port.in.RegisterExerciseUseCase;
import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import com.diabecare.presentation.dto.request.RegisterExerciseRequest;
import com.diabecare.presentation.dto.response.ExerciseLogResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exercise")
@RequiredArgsConstructor
public class ExerciseController {

    private final RegisterExerciseUseCase registerExerciseUseCase;
    private final GetExerciseHistoryUseCase getExerciseHistoryUseCase;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{patientId}")
    public ResponseEntity<ExerciseLogResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody RegisterExerciseRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        LocalDateTime performedAt = request.performedAt() != null
                ? LocalDateTime.parse(request.performedAt()) : null;

        ExerciseLog log = registerExerciseUseCase.execute(
                new RegisterExerciseUseCase.Command(
                        patientId,
                        ExerciseType.valueOf(request.exerciseType()),
                        ExerciseIntensity.valueOf(request.intensity()),
                        request.durationMinutes(),
                        request.notes(),
                        performedAt,
                        request.caloriesBurned()
                ));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(log));
    }

    @GetMapping("/{patientId}/history")
    public ResponseEntity<List<ExerciseLogResponse>> getHistory(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(
                getExerciseHistoryUseCase.getHistory(patientId, from, to)
                        .stream().map(this::toResponse).toList()
        );
    }

    private ExerciseLogResponse toResponse(ExerciseLog log) {
        return new ExerciseLogResponse(
                log.getExerciseId(),
                log.getExerciseType().name(),
                log.getIntensity().name(),
                log.getDurationMinutes(),
                log.getCaloriesBurned(),
                log.getNotes(),
                log.getPerformedAt().toString()
        );
    }
}