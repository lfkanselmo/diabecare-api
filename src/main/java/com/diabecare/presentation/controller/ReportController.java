package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GenerateMedicalReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final GenerateMedicalReportUseCase generateMedicalReportUseCase;

    @GetMapping("/{patientId}/medical")
    public ResponseEntity<byte[]> generateMedicalReport(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        byte[] pdf = generateMedicalReportUseCase.generate(
                new GenerateMedicalReportUseCase.Command(patientId, from, to));

        String filename = "DiabeCare_Reporte_" +
                from.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "_" +
                to.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}