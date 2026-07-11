package com.diabecare.application.port.out;

import com.diabecare.domain.model.ExerciseLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LoadExerciseLogPort {
    // Rango completo, sin paginar: usado por correlación con glucosa y exportación.
    List<ExerciseLog> findByPatientIdAndDateRange(UUID patientId,
                                                  LocalDateTime from,
                                                  LocalDateTime to);
    Page<ExerciseLog> findByPatientIdAndDateRange(UUID patientId,
                                                  LocalDateTime from,
                                                  LocalDateTime to,
                                                  Pageable pageable);

    // Cursor de sincronización incremental para el móvil offline-first — ver
    // ExerciseLogJpaRepository.findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc.
    List<ExerciseLog> findByPatientIdUpdatedAfter(UUID patientId, LocalDateTime since);
}