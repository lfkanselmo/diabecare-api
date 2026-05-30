package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GenerateMedicalReportUseCase;
import com.diabecare.application.port.out.*;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.MedicalCalculatorService;
import com.diabecare.domain.service.ReportDataService;
import com.diabecare.infrastructure.pdf.MedicalReportPdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GenerateMedicalReportUseCaseImpl implements GenerateMedicalReportUseCase {

    private final LoadPatientPort loadPatientPort;
    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final LoadMealEntryPort loadMealEntryPort;
    private final LoadVitalSignPort loadVitalSignPort;
    private final LoadMedicationPort loadMedicationPort;
    private final MedicalCalculatorService medicalCalculatorService;
    private final MedicalReportPdfGenerator pdfGenerator;

    @Override
    public byte[] generate(Command command) {
        Patient patient = loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        LocalDateTime from = command.from().atStartOfDay();
        LocalDateTime to   = command.to().atTime(23, 59, 59);

        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(command.patientId(), from, to);

        List<MealEntry> meals = loadMealEntryPort
                .findByPatientIdAndDateRange(command.patientId(),
                        command.from(), command.to());

        List<VitalSign> vitals = loadVitalSignPort
                .findByPatientId(command.patientId());

        List<Medication> medications = loadMedicationPort
                .findActiveByPatientId(command.patientId());

        ReportDataService reportData = ReportDataService.builder()
                .patient(patient)
                .glucoseReadings(readings)
                .mealEntries(meals)
                .vitalSigns(vitals)
                .medications(medications)
                .averageGlucose(medicalCalculatorService.calculateAverage(readings))
                .estimatedHba1c(readings.isEmpty() ? null :
                        medicalCalculatorService.estimateHba1c(
                                medicalCalculatorService.calculateAverage(readings)))
                .timeInRangePercent(medicalCalculatorService.calculateTimeInRange(
                        readings, patient.getTargetGlucoseMin(),
                        patient.getTargetGlucoseMax()))
                .coefficientOfVariation(readings.isEmpty() ? null :
                        medicalCalculatorService.calculateCoefficientOfVariation(
                                medicalCalculatorService.calculateStandardDeviation(readings),
                                medicalCalculatorService.calculateAverage(readings)))
                .build();

        return pdfGenerator.generate(reportData, command.from(), command.to());
    }
}