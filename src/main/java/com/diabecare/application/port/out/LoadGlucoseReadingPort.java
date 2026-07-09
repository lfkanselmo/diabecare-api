package com.diabecare.application.port.out;

import com.diabecare.domain.model.GlucoseReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadGlucoseReadingPort {
    Optional<GlucoseReading> findById(UUID readingId);
    Optional<GlucoseReading> findLatestByPatientId(UUID patientId);
    // Rango completo, sin paginar: usado por estadísticas/AGP/alertas/exportación.
    List<GlucoseReading> findByPatientIdAndDateRange(UUID patientId,
                                                     LocalDateTime from,
                                                     LocalDateTime to);
    Page<GlucoseReading> findByPatientIdAndDateRange(UUID patientId,
                                                     LocalDateTime from,
                                                     LocalDateTime to,
                                                     Pageable pageable);
    void deleteById(UUID readingId);

    // Ver GlucoseReadingJpaRepository.findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc.
    List<GlucoseReading> findByPatientIdUpdatedAfter(UUID patientId, LocalDateTime since);
}