package com.diabecare.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ImportGlucoseReadingsRequest(
        @NotEmpty @Valid
        List<Entry> readings
) {
    public record Entry(
            @NotNull @DecimalMin("20") @DecimalMax("600")
            BigDecimal value,

            @NotNull
            String unit,

            @NotNull
            String readingType,

            @NotNull
            LocalDateTime measuredAt
    ) {}
}
