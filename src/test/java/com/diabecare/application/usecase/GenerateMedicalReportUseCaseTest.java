package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GenerateMedicalReportUseCase;
import com.diabecare.application.port.out.*;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.CycleStatisticsService;
import com.diabecare.domain.service.MedicalCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateMedicalReportUseCaseImpl")
class GenerateMedicalReportUseCaseTest {

    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock
    private LoadMealEntryPort loadMealEntryPort;
    @Mock
    private LoadVitalSignPort loadVitalSignPort;
    @Mock
    private LoadMedicationPort loadMedicationPort;
    @Mock
    private LoadExerciseLogPort loadExerciseLogPort;
    @Mock
    private LoadMenstrualCyclePort loadMenstrualCyclePort;
    @Mock
    private GenerateReportPort generateReportPort;

    private GenerateMedicalReportUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();
    private final LocalDate from = LocalDate.of(2026, 6, 1);
    private final LocalDate to = LocalDate.of(2026, 6, 7);

    @BeforeEach
    void setUp() {
        useCase = new GenerateMedicalReportUseCaseImpl(
                loadPatientPort, loadGlucoseReadingPort, loadMealEntryPort, loadVitalSignPort,
                loadMedicationPort, loadExerciseLogPort, loadMenstrualCyclePort,
                new MedicalCalculatorService(), new CycleStatisticsService(), generateReportPort);
    }

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            GenerateMedicalReportUseCase.Command command =
                    new GenerateMedicalReportUseCase.Command(patientId, from, to);

            assertThatThrownBy(() -> useCase.generate(command))
                    .isInstanceOf(PatientNotFoundException.class);

            verifyNoInteractions(generateReportPort);
        }

        @Test
        @DisplayName("retorna los bytes producidos por el puerto de generación de reportes")
        void returnsBytesFromReportPort() {
            stubEmptyDataFor(validPatient());
            byte[] expectedBytes = new byte[]{1, 2, 3};
            when(generateReportPort.generate(any(), eq(from), eq(to))).thenReturn(expectedBytes);

            byte[] result = useCase.generate(new GenerateMedicalReportUseCase.Command(patientId, from, to));

            assertThat(result).isEqualTo(expectedBytes);
        }

        @Test
        @DisplayName("no incluye datos de ciclo menstrual cuando el paciente no es de sexo femenino")
        void doesNotIncludeCycleDataWhenPatientNotFemale() {
            stubEmptyDataFor(validPatient());

            useCase.generate(new GenerateMedicalReportUseCase.Command(patientId, from, to));

            ArgumentCaptor<ReportData> captor = ArgumentCaptor.forClass(ReportData.class);
            verify(generateReportPort).generate(captor.capture(), eq(from), eq(to));

            assertThat(captor.getValue().getLatestMenstrualCycle()).isNull();
            verifyNoInteractions(loadMenstrualCyclePort);
        }

        @Test
        @DisplayName("incluye el último ciclo y los promedios cuando el paciente es de sexo femenino")
        void includesCycleDataWhenPatientIsFemale() {
            Patient patient = femalePatient();
            stubEmptyDataFor(patient);

            MenstrualCycle cycle = MenstrualCycle.startNewCycle(patientId, LocalDate.of(2026, 5, 1), null);
            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of(cycle));

            useCase.generate(new GenerateMedicalReportUseCase.Command(patientId, from, to));

            ArgumentCaptor<ReportData> captor = ArgumentCaptor.forClass(ReportData.class);
            verify(generateReportPort).generate(captor.capture(), eq(from), eq(to));

            assertThat(captor.getValue().getLatestMenstrualCycle()).isEqualTo(cycle);
        }

        @Test
        @DisplayName("limita las comidas de mayor impacto a 5, ordenadas por calorías descendente")
        void limitsTopImpactMealsToFiveSortedByCaloriesDescending() {
            Patient patient = validPatient();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(List.of());
            when(loadVitalSignPort.findByPatientId(patientId)).thenReturn(List.of());
            when(loadMedicationPort.findActiveByPatientId(patientId)).thenReturn(List.of());
            when(loadExerciseLogPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(List.of());

            List<MealEntry> meals = List.of(
                    mealWithCalories(100), mealWithCalories(500), mealWithCalories(300),
                    mealWithCalories(700), mealWithCalories(200), mealWithCalories(600));
            when(loadMealEntryPort.findByPatientIdAndDateRange(patientId, from, to)).thenReturn(meals);

            useCase.generate(new GenerateMedicalReportUseCase.Command(patientId, from, to));

            ArgumentCaptor<ReportData> captor = ArgumentCaptor.forClass(ReportData.class);
            verify(generateReportPort).generate(captor.capture(), eq(from), eq(to));

            List<MealEntry> topMeals = captor.getValue().getTopImpactMeals();
            assertThat(topMeals).hasSize(5);
            assertThat(topMeals.get(0).getTotalCalories()).isEqualByComparingTo(BigDecimal.valueOf(700));
            assertThat(topMeals.get(4).getTotalCalories()).isEqualByComparingTo(BigDecimal.valueOf(200));
        }

        @Test
        @DisplayName("no calcula hba1c ni coeficiente de variación cuando no hay lecturas de glucosa")
        void doesNotCalculateHba1cOrCvWhenNoReadings() {
            stubEmptyDataFor(validPatient());

            useCase.generate(new GenerateMedicalReportUseCase.Command(patientId, from, to));

            ArgumentCaptor<ReportData> captor = ArgumentCaptor.forClass(ReportData.class);
            verify(generateReportPort).generate(captor.capture(), eq(from), eq(to));

            assertThat(captor.getValue().getEstimatedHba1c()).isNull();
            assertThat(captor.getValue().getCoefficientOfVariation()).isNull();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void stubEmptyDataFor(Patient patient) {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(List.of());
        when(loadMealEntryPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(List.of());
        when(loadVitalSignPort.findByPatientId(patientId)).thenReturn(List.of());
        when(loadMedicationPort.findActiveByPatientId(patientId)).thenReturn(List.of());
        when(loadExerciseLogPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(List.of());
    }

    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }

    private Patient femalePatient() {
        Patient patient = validPatient();
        patient.updateBiologicalSex(BiologicalSex.FEMALE);
        return patient;
    }

    private MealEntry mealWithCalories(double calories) {
        MealEntry meal = MealEntry.create(patientId, MealType.LUNCH, LocalDateTime.of(from, java.time.LocalTime.NOON), null);
        meal.addItem(MealItem.create("Comida", BigDecimal.valueOf(100),
                BigDecimal.valueOf(calories), BigDecimal.valueOf(10), null, null, null));
        return meal;
    }
}