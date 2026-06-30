package com.diabecare.infrastructure.pdf;

import com.diabecare.domain.model.*;
import com.diabecare.domain.service.ExerciseLabelService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MedicalReportPdfGenerator")
class MedicalReportPdfGeneratorTest {

    private MedicalReportPdfGenerator generator;
    private final UUID patientId = UUID.randomUUID();
    private final LocalDate from = LocalDate.of(2026, 6, 1);
    private final LocalDate to = LocalDate.of(2026, 6, 7);

    @BeforeEach
    void setUp() {
        generator = new MedicalReportPdfGenerator(
                new MenstrualCycleGuidanceService((key, args) -> "texto de guía"),
                new ExerciseLabelService());
    }

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("genera un PDF válido (con cabecera %PDF) con datos completos")
        void generatesValidPdfWithCompleteData() {
            ReportData data = fullReportData();

            byte[] result = generator.generate(data, from, to);

            assertThat(result).isNotEmpty();
            String header = new String(result, 0, 4, java.nio.charset.StandardCharsets.US_ASCII);
            assertThat(header).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("genera un PDF válido cuando las listas están vacías y los valores agregados son nulos")
        void generatesValidPdfWithEmptyListsAndNullAggregates() {
            ReportData data = emptyReportData(null);

            byte[] result = generator.generate(data, from, to);

            assertThat(result).isNotEmpty();
            String header = new String(result, 0, 4, java.nio.charset.StandardCharsets.US_ASCII);
            assertThat(header).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("genera un PDF válido cuando no hay ciclo menstrual (paciente no femenina)")
        void generatesValidPdfWithoutMenstrualCycle() {
            ReportData data = emptyReportData(null);

            byte[] result = generator.generate(data, from, to);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("genera un PDF válido cuando hay episodios de hipoglucemia registrados")
        void generatesValidPdfWithHypoglycemiaEvents() {
            GlucoseReading hypoEvent = GlucoseReading.create(
                    patientId, BigDecimal.valueOf(60), GlucoseUnit.MG_DL,
                    ReadingType.FASTING, LocalDateTime.now().minusDays(1), null, null);

            ReportData data = ReportData.builder()
                    .patient(validPatient())
                    .glucoseReadings(List.of())
                    .mealEntries(List.of())
                    .vitalSigns(List.of())
                    .medications(List.of())
                    .exerciseLogs(List.of())
                    .latestMenstrualCycle(null)
                    .averageCycleLength(null)
                    .averagePeriodLength(null)
                    .estimatedHba1c(null)
                    .timeInRangePercent(null)
                    .averageGlucose(null)
                    .coefficientOfVariation(null)
                    .tirDetailed(Map.of())
                    .averageByReadingType(Map.of())
                    .hypoglycemiaEvents(List.of(hypoEvent))
                    .adherencePercent(0.0)
                    .topImpactMeals(List.of())
                    .build();

            byte[] result = generator.generate(data, from, to);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("genera un PDF válido cuando sí hay ciclo menstrual (paciente femenina)")
        void generatesValidPdfWithMenstrualCycle() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 5, 15), null);
            ReportData data = emptyReportData(cycle);

            byte[] result = generator.generate(data, from, to);

            assertThat(result).isNotEmpty();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ReportData fullReportData() {
        Patient patient = validPatient();
        GlucoseReading reading = GlucoseReading.create(
                patientId, BigDecimal.valueOf(120), GlucoseUnit.MG_DL,
                ReadingType.FASTING, LocalDateTime.now().minusDays(1), null, null);

        MealEntry meal = MealEntry.create(
                patientId, MealType.BREAKFAST, LocalDateTime.now().minusDays(1), null);
        meal.addItem(MealItem.create("Pan", BigDecimal.valueOf(50),
                BigDecimal.valueOf(200), BigDecimal.valueOf(40), BigDecimal.valueOf(5), BigDecimal.valueOf(2), null));

        VitalSign vital = VitalSign.builder()
                .vitalId(UUID.randomUUID())
                .patientId(patientId)
                .weightKg(BigDecimal.valueOf(65))
                .measuredAt(LocalDateTime.now().minusDays(1))
                .build();

        Medication medication = Medication.create(
                patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), null);

        ExerciseLog exercise = ExerciseLog.create(
                patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                30, null, LocalDateTime.now().minusDays(1), BigDecimal.valueOf(150));

        MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 5, 15), null);

        return ReportData.builder()
                .patient(patient)
                .glucoseReadings(List.of(reading))
                .mealEntries(List.of(meal))
                .vitalSigns(List.of(vital))
                .medications(List.of(medication))
                .exerciseLogs(List.of(exercise))
                .latestMenstrualCycle(cycle)
                .averageCycleLength(28)
                .averagePeriodLength(5)
                .estimatedHba1c(BigDecimal.valueOf(6.5))
                .timeInRangePercent(BigDecimal.valueOf(75))
                .averageGlucose(BigDecimal.valueOf(120))
                .coefficientOfVariation(BigDecimal.valueOf(30))
                .tirDetailed(Map.of("inRange", BigDecimal.valueOf(75)))
                .averageByReadingType(Map.of("FASTING", BigDecimal.valueOf(110)))
                .hypoglycemiaEvents(List.of())
                .adherencePercent(85.0)
                .topImpactMeals(List.of(meal))
                .build();
    }

    private ReportData emptyReportData(MenstrualCycle cycle) {
        return ReportData.builder()
                .patient(validPatient())
                .glucoseReadings(List.of())
                .mealEntries(List.of())
                .vitalSigns(List.of())
                .medications(List.of())
                .exerciseLogs(List.of())
                .latestMenstrualCycle(cycle)
                .averageCycleLength(null)
                .averagePeriodLength(null)
                .estimatedHba1c(null)
                .timeInRangePercent(null)
                .averageGlucose(null)
                .coefficientOfVariation(null)
                .tirDetailed(Map.of())
                .averageByReadingType(Map.of())
                .hypoglycemiaEvents(List.of())
                .adherencePercent(0.0)
                .topImpactMeals(List.of())
                .build();
    }

    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}