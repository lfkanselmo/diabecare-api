package com.diabecare.application.port.out;

import com.diabecare.domain.model.ReportData;

import java.time.LocalDate;

public interface GenerateReportPort {
    byte[] generate(ReportData data, LocalDate from, LocalDate to);
}