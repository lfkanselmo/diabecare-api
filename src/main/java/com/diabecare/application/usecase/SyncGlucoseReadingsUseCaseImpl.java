package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SyncGlucoseReadingsUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SyncGlucoseReadingsUseCaseImpl implements SyncGlucoseReadingsUseCase {

    // "Época" segura muy anterior a cualquier dato real — evita pasar null a la
    // consulta (un "updated_at > NULL" en SQL no matchea ninguna fila) y evita
    // LocalDateTime.MIN, que está fuera del rango válido de un TIMESTAMP de Postgres.
    private static final LocalDateTime BEGINNING_OF_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;

    @Override
    public List<GlucoseReading> execute(UUID patientId, LocalDateTime since) {
        return loadGlucoseReadingPort.findByPatientIdUpdatedAfter(
                patientId, since != null ? since : BEGINNING_OF_TIME);
    }
}
