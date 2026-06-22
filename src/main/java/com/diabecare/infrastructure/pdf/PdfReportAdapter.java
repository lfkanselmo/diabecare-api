package com.diabecare.infrastructure.pdf;

import com.diabecare.application.port.out.GenerateReportPort;
import com.diabecare.domain.model.ReportData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PdfReportAdapter implements GenerateReportPort {

    private final MedicalReportPdfGenerator generator;

    @Override
    public byte[] generate(ReportData data, LocalDate from, LocalDate to) {
        return generator.generate(data, from, to);
    }
}