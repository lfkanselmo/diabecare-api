package com.diabecare.application.port.in;

import com.diabecare.application.dto.DailySummaryRecord;

import java.time.LocalDate;
import java.util.UUID;

public interface GetDailySummaryUseCase {
    DailySummaryRecord getSummary(UUID patientId, LocalDate date);
}