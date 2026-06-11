package com.diabecare.application.port.out;

import com.diabecare.domain.service.ReportDataService;

import java.time.LocalDate;

public interface GenerateReportPort {
    byte[] generate(ReportDataService data, LocalDate from, LocalDate to);
}