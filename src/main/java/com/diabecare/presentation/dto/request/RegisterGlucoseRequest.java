package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterGlucoseRequest(
        @NotNull @DecimalMin("20") @DecimalMax("600")
        BigDecimal value,

        @NotNull
        String unit,

        @NotNull
        String readingType,

        @NotNull
        LocalDateTime measuredAt,

        String notes,
        String deviceSource,

        // Opcional — un cliente offline-first (app móvil) genera su propio UUID para
        // poder mostrar el registro antes de sincronizar. Si no viene, el servidor
        // genera uno (comportamiento actual, usado por el frontend web).
        UUID readingId
) {}