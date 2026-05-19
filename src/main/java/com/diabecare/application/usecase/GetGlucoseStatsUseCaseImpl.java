package com.diabecare.application.usecase;

import com.diabecare.application.dto.GlucoseStatsRecord;
import com.diabecare.application.port.in.GetGlucoseStatsUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseStatus;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.MedicalCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetGlucoseStatsUseCaseImpl implements GetGlucoseStatsUseCase {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final LoadPatientPort loadPatientPort;
    private final MedicalCalculatorService medicalCalculatorService;

    @Override
    public GlucoseStatsRecord getStats(UUID patientId, LocalDateTime from, LocalDateTime to) {
        Patient patient = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));

        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patientId, from, to);

        if (readings.isEmpty()) {
            return emptyStats();
        }

        BigDecimal average = medicalCalculatorService.calculateAverage(readings);
        BigDecimal stdDev = medicalCalculatorService.calculateStandardDeviation(readings);
        BigDecimal cv = medicalCalculatorService.calculateCoefficientOfVariation(stdDev, average);
        BigDecimal hba1c = medicalCalculatorService.estimateHba1c(average);
        BigDecimal tir = medicalCalculatorService.calculateTimeInRange(
                readings, patient.getTargetGlucoseMin(), patient.getTargetGlucoseMax());
        BigDecimal tbr = medicalCalculatorService.calculateTimeByStatus(
                readings, GlucoseStatus.LOW);
        BigDecimal tar = medicalCalculatorService.calculateTimeByStatus(
                readings, GlucoseStatus.HIGH);

        return new GlucoseStatsRecord(
                average, stdDev, cv, hba1c, tir, tbr, tar, readings.size());
    }

    private GlucoseStatsRecord emptyStats() {
        return new GlucoseStatsRecord(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, 0);
    }
}