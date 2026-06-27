package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GenerateMedicalReportUseCase;
import com.diabecare.application.port.out.*;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.CycleStatisticsService;
import com.diabecare.domain.service.MedicalCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateMedicalReportUseCaseImpl implements GenerateMedicalReportUseCase {

    private final LoadPatientPort          loadPatientPort;
    private final LoadGlucoseReadingPort   loadGlucoseReadingPort;
    private final LoadMealEntryPort        loadMealEntryPort;
    private final LoadVitalSignPort        loadVitalSignPort;
    private final LoadMedicationPort       loadMedicationPort;
    private final LoadExerciseLogPort      loadExerciseLogPort;
    private final LoadMenstrualCyclePort   loadMenstrualCyclePort;
    private final MedicalCalculatorService medicalCalculatorService;
    private final CycleStatisticsService   cycleStatisticsService;
    private final GenerateReportPort       generateReportPort;

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

        List<ExerciseLog> exercises = loadExerciseLogPort
                .findByPatientIdAndDateRange(command.patientId(), from, to);

        MenstrualCycle latestCycle = null;
        Integer avgCycleLength = null;
        Integer avgPeriodLength = null;
        if (patient.isFemale()) {
            List<MenstrualCycle> cycleHistory = loadMenstrualCyclePort
                    .findByPatientId(command.patientId());
            latestCycle = cycleHistory.stream().findFirst().orElse(null);
            avgCycleLength = cycleStatisticsService.calculateAverageCycleLength(cycleHistory);
            avgPeriodLength = cycleStatisticsService.calculateAveragePeriodLength(cycleHistory);
        }

        BigDecimal avg = medicalCalculatorService.calculateAverage(readings);

        List<MealEntry> topMeals = meals.stream()
                .sorted((a, b) -> b.getTotalCalories().compareTo(a.getTotalCalories()))
                .limit(5)
                .toList();

        ReportData reportData = ReportData.builder()
                .patient(patient)
                .glucoseReadings(readings)
                .mealEntries(meals)
                .vitalSigns(vitals)
                .medications(medications)
                .exerciseLogs(exercises)
                .latestMenstrualCycle(latestCycle)
                .averageCycleLength(avgCycleLength)
                .averagePeriodLength(avgPeriodLength)
                .averageGlucose(avg)
                .estimatedHba1c(readings.isEmpty() ? null :
                        medicalCalculatorService.estimateHba1c(avg))
                .timeInRangePercent(medicalCalculatorService.calculateTimeInRange(
                        readings, patient.getTargetGlucoseMin(), patient.getTargetGlucoseMax()))
                .coefficientOfVariation(readings.isEmpty() ? null :
                        medicalCalculatorService.calculateCoefficientOfVariation(
                                medicalCalculatorService.calculateStandardDeviation(readings), avg))
                .tirDetailed(medicalCalculatorService.calculateTirDetailed(readings))
                .averageByReadingType(medicalCalculatorService.calculateAverageByReadingType(readings))
                .hypoglycemiaEvents(medicalCalculatorService.getHypoglycemiaEvents(readings))
                .adherencePercent(medicalCalculatorService.calculateAdherencePercent(readings, from, to))
                .topImpactMeals(topMeals)
                .build();

        return generateReportPort.generate(reportData, command.from(), command.to());
    }
}