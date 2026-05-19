package com.diabecare.application.usecase;

import com.diabecare.application.dto.DailySummaryRecord;
import com.diabecare.application.port.in.GetDailySummaryUseCase;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetDailySummaryUseCaseImpl implements GetDailySummaryUseCase {

    private final LoadMealEntryPort loadMealEntryPort;
    private final LoadPatientPort loadPatientPort;

    @Override
    public DailySummaryRecord getSummary(UUID patientId, LocalDate date) {
        Patient patient = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));

        List<MealEntry> meals = loadMealEntryPort.findByPatientIdAndDate(patientId, date);

        BigDecimal totalCalories = meals.stream()
                .map(MealEntry::getTotalCalories)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCarbs = meals.stream()
                .map(MealEntry::getTotalCarbohydrates)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProteins = meals.stream()
                .map(MealEntry::getTotalProteins)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFats = meals.stream()
                .map(MealEntry::getTotalFats)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer calorieGoal = patient.getDailyCalorieGoal();
        boolean goalReached = calorieGoal != null
                && totalCalories.compareTo(BigDecimal.valueOf(calorieGoal)) <= 0;

        return new DailySummaryRecord(
                date, totalCalories, totalCarbs, totalProteins,
                totalFats, calorieGoal, goalReached);
    }
}