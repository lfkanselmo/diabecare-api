package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetExerciseHistoryUseCase;
import com.diabecare.application.port.in.RegisterExerciseUseCase;
import com.diabecare.application.port.in.SyncExerciseLogsUseCase;
import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import com.diabecare.presentation.dto.request.RegisterExerciseRequest;
import com.diabecare.presentation.dto.response.ExerciseLogResponse;
import com.diabecare.presentation.dto.response.PageResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    private final SyncExerciseLogsUseCase syncExerciseLogsUseCase;
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
                        request.caloriesBurned(),
                        request.exerciseId()
                ));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(log));
    }

    @GetMapping("/{patientId}/history")
    public ResponseEntity<PageResponse<ExerciseLogResponse>> getHistory(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(PageResponse.of(
                getExerciseHistoryUseCase.getHistory(patientId, from, to, PageRequest.of(page, size)),
                this::toResponse));
    }

    /**
     * Cursor de sincronización incremental para el motor offline-first del móvil —
     * distinto de /history (que pagina por fecha de rutina para la UI web).
     * {@code since} ausente trae el historial completo (primera sincronización).
     */
    @GetMapping("/{patientId}/sync")
    public ResponseEntity<List<ExerciseLogResponse>> sync(
            @PathVariable UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        List<ExerciseLogResponse> logs = syncExerciseLogsUseCase.execute(patientId, since).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(logs);
    }

    private ExerciseLogResponse toResponse(ExerciseLog log) {
        return new ExerciseLogResponse(
                log.getExerciseId(),
                log.getExerciseType().name(),
                log.getIntensity().name(),
                log.getDurationMinutes(),
                log.getCaloriesBurned(),
                log.getNotes(),
                log.getPerformedAt().toString(),
                log.getUpdatedAt()
        );
    }
}