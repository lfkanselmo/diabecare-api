package com.diabecare.domain.service;

import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.Alert;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class NoMealAlertDetector implements AlertDetector {

    private final LoadMealEntryPort loadMealEntryPort;
    private final MessageResolverPort messages;

    @Override
    public List<Alert> detect(Patient patient, LocalDateTime now) {
        List<MealEntry> todayMeals = loadMealEntryPort
                .findByPatientIdAndDate(patient.getPatientId(), now.toLocalDate());

        if (todayMeals.isEmpty()) {
            return List.of(Alert.builder()
                    .type(Alert.AlertType.NO_MEAL_RECORDED)
                    .severity(Alert.Severity.INFO)
                    .title(messages.resolve("alert.no-meal.title"))
                    .message(messages.resolve("alert.no-meal.message"))
                    .build());
        }

        return List.of();
    }
}
