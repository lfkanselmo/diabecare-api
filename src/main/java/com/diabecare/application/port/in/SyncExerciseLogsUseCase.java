package com.diabecare.application.port.in;

import com.diabecare.domain.model.ExerciseLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cursor de sincronización incremental para el motor offline-first del móvil —
 * mismo patrón que {@link SyncGlucoseReadingsUseCase}.
 */
public interface SyncExerciseLogsUseCase {

    /** {@code since} nulo trae el historial completo del paciente (primera sincronización). */
    List<ExerciseLog> execute(UUID patientId, LocalDateTime since);
}
