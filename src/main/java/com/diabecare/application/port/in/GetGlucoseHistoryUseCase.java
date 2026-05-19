package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReading;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GetGlucoseHistoryUseCase {
    List<GlucoseReading> getByPatientAndDateRange(UUID patientId,
                                                  LocalDateTime from,
                                                  LocalDateTime to);
}