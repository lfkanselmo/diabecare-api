package com.diabecare.application.port.in;

import com.diabecare.domain.model.MealEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cursor de sincronización incremental para el motor offline-first del móvil —
 * mismo patrón que {@link SyncGlucoseReadingsUseCase}. Distinto de
 * {@link GetMealHistoryUseCase}, que pagina por fecha de consumo para la UI web.
 */
public interface SyncMealEntriesUseCase {

    /** {@code since} nulo trae el historial completo del paciente (primera sincronización). */
    List<MealEntry> execute(UUID patientId, LocalDateTime since);
}
