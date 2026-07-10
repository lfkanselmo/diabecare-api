package com.diabecare.application.usecase;

import com.diabecare.application.dto.DailySummaryRecord;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;
import com.diabecare.domain.model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
@DisplayName("GetDailySummaryUseCaseImpl")
class GetDailySummaryUseCaseTest {

    @Mock
    private LoadMealEntryPort loadMealEntryPort;
    @Mock
    private LoadPatientPort loadPatientPort;

    @InjectMocks
    private GetDailySummaryUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();
    private final LocalDate date = LocalDate.of(2026, 6, 15);

    @Nested
    @DisplayName("getSummary")
    class GetSummary {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getSummary(patientId, date))
                    .isInstanceOf(PatientNotFoundException.class);
        }

        @Test
        @DisplayName("suma correctamente los 4 totales nutricionales de varias comidas")
        void sumsAllFourNutritionalTotalsAcrossMeals() {
            Patient patient = validPatient();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            MealEntry breakfast = mealWith(MealType.BREAKFAST, "Pan", 100, 20, 5, 2);
            MealEntry lunch = mealWith(MealType.LUNCH, "Arroz", 200, 40, 5, 3);
            when(loadMealEntryPort.findByPatientIdAndDate(patientId, date))
                    .thenReturn(List.of(breakfast, lunch));

            DailySummaryRecord summary = useCase.getSummary(patientId, date);

            assertThat(summary.totalCalories()).isEqualByComparingTo(BigDecimal.valueOf(300));
            assertThat(summary.totalCarbohydrates()).isEqualByComparingTo(BigDecimal.valueOf(60));
            assertThat(summary.totalProteins()).isEqualByComparingTo(BigDecimal.valueOf(10));
            assertThat(summary.totalFats()).isEqualByComparingTo(BigDecimal.valueOf(5));
        }

        @Test
        @DisplayName("retorna totales en cero cuando no hay comidas registradas ese día")
        void returnsZeroTotalsWhenNoMeals() {
            Patient patient = validPatient();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
            when(loadMealEntryPort.findByPatientIdAndDate(patientId, date)).thenReturn(List.of());

            DailySummaryRecord summary = useCase.getSummary(patientId, date);

            assertThat(summary.totalCalories()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.totalCarbohydrates()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("marca la meta como alcanzada cuando el total de calorías es igual a la meta")
        void marksGoalReachedWhenCaloriesEqualGoal() {
            Patient patient = validPatient();
            patient.updateDailyCalorieGoal(600);
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            MealEntry meal = mealWith(MealType.LUNCH, "Comida", 600, 50, 10, 5);
            when(loadMealEntryPort.findByPatientIdAndDate(patientId, date)).thenReturn(List.of(meal));

            DailySummaryRecord summary = useCase.getSummary(patientId, date);

            assertThat(summary.goalReached()).isTrue();
        }

        @Test
        @DisplayName("marca la meta como no alcanzada cuando el total de calorías supera la meta")
        void marksGoalNotReachedWhenCaloriesExceedGoal() {
            Patient patient = validPatient();
            patient.updateDailyCalorieGoal(600);
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            MealEntry meal = mealWith(MealType.LUNCH, "Comida", 650, 50, 10, 5);
            when(loadMealEntryPort.findByPatientIdAndDate(patientId, date)).thenReturn(List.of(meal));

            DailySummaryRecord summary = useCase.getSummary(patientId, date);

            assertThat(summary.goalReached()).isFalse();
        }

        @Test
        @DisplayName("marca la meta como no alcanzada cuando el paciente no tiene meta calórica configurada")
        void marksGoalNotReachedWhenNoCalorieGoalConfigured() {
            Patient patient = validPatient(); // sin meta calórica
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            MealEntry meal = mealWith(MealType.LUNCH, "Comida", 100, 20, 5, 2);
            when(loadMealEntryPort.findByPatientIdAndDate(patientId, date)).thenReturn(List.of(meal));

            DailySummaryRecord summary = useCase.getSummary(patientId, date);

            assertThat(summary.goalReached()).isFalse();
            assertThat(summary.calorieGoal()).isNull();
        }

        @Test
        @DisplayName("incluye la fecha consultada en el resultado")
        void includesQueriedDateInResult() {
            Patient patient = validPatient();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
            when(loadMealEntryPort.findByPatientIdAndDate(patientId, date)).thenReturn(List.of());

            DailySummaryRecord summary = useCase.getSummary(patientId, date);

            assertThat(summary.date()).isEqualTo(date);
        }
    }


    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }

    private MealEntry mealWith(MealType type, String foodName, double calories,
                               double carbs, double proteins, double fats) {
        MealEntry meal = MealEntry.create(patientId, type, LocalDateTime.of(date, java.time.LocalTime.NOON), null);
        meal.addItem(MealItem.create(foodName, BigDecimal.valueOf(100),
                BigDecimal.valueOf(calories), BigDecimal.valueOf(carbs),
                BigDecimal.valueOf(proteins), BigDecimal.valueOf(fats), null));
        return meal;
    }
}
