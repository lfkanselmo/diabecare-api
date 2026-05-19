package com.diabecare.application.port.in;

import com.diabecare.application.dto.GlucoseStatsRecord;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GetGlucoseStatsUseCase {
    GlucoseStatsRecord getStats(UUID patientId, LocalDateTime from, LocalDateTime to);
}