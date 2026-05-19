package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterGlucoseReadingUseCase {

    record Command(
            UUID patientId,
            BigDecimal value,
            GlucoseUnit unit,
            ReadingType readingType,
            LocalDateTime measuredAt,
            String notes,
            String deviceSource
    ) {}

    GlucoseReading execute(Command command);
}