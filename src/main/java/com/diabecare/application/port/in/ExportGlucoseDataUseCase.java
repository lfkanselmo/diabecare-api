package com.diabecare.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ExportGlucoseDataUseCase {
    String exportAsCsv(UUID patientId, LocalDateTime from, LocalDateTime to);
    String exportAsJson(UUID patientId, LocalDateTime from, LocalDateTime to);
}