package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto de entrada para que un bridge externo (CGM, Nightscout, un script de
 * sincronización de un glucómetro) importe lecturas sin una sesión JWT interactiva —
 * se autentica con una {@link com.diabecare.domain.model.DeviceApiKey} en vez de un
 * usuario/contraseña. Reutiliza {@link RegisterGlucoseReadingUseCase} para la validación
 * y persistencia real, así que ambos caminos comparten exactamente las mismas reglas.
 */
public interface ImportGlucoseReadingsUseCase {

    record ReadingInput(
            BigDecimal value,
            GlucoseUnit unit,
            ReadingType readingType,
            LocalDateTime measuredAt
    ) {}

    record Command(String rawApiKey, List<ReadingInput> readings) {}

    List<GlucoseReading> execute(Command command);
}
