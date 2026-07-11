package com.diabecare.application.port.in;

import com.diabecare.domain.model.Medication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cursor de sincronización incremental para el motor offline-first del móvil —
 * mismo patrón que {@link SyncGlucoseReadingsUseCase}. A diferencia de
 * {@link GetMedicationsUseCase} (solo activos), acá se incluyen también los
 * desactivados: el cliente offline necesita enterarse de una desactivación
 * ocurrida en otro dispositivo, no solo de altas nuevas.
 */
public interface SyncMedicationsUseCase {

    /** {@code since} nulo trae el historial completo del paciente (primera sincronización). */
    List<Medication> execute(UUID patientId, LocalDateTime since);
}
