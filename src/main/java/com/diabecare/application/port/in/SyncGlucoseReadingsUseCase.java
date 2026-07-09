package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReading;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cursor de sincronización incremental para el motor offline-first del móvil —
 * ver ARCHITECTURE.md de diabecare-mobile, sección 3.3. Distinto de
 * {@link GetGlucoseHistoryUseCase}, que pagina por rango de fecha de medición
 * para la UI del historial.
 */
public interface SyncGlucoseReadingsUseCase {

    /** {@code since} nulo trae el historial completo del paciente (primera sincronización). */
    List<GlucoseReading> execute(UUID patientId, LocalDateTime since);
}
