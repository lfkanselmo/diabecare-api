package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ExportGlucoseDataUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.service.GlucoseExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportGlucoseDataUseCaseImpl implements ExportGlucoseDataUseCase {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final GlucoseExportService   glucoseExportService;

    @Override
    public String exportAsCsv(UUID patientId, LocalDateTime from, LocalDateTime to) {
        List<GlucoseReading> readings =
                loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to);
        return glucoseExportService.toCsv(readings);
    }

    @Override
    public String exportAsJson(UUID patientId, LocalDateTime from, LocalDateTime to) {
        List<GlucoseReading> readings =
                loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to);
        return glucoseExportService.toJson(readings);
    }
}