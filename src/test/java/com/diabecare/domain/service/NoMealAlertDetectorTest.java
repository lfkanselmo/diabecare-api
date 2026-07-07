package com.diabecare.domain.service;

import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoMealAlertDetector")
class NoMealAlertDetectorTest {

    @Mock private LoadMealEntryPort loadMealEntryPort;

    private NoMealAlertDetector detector;
    private final UUID patientId = UUID.randomUUID();
    private final Patient patient = Patient.builder()
            .patientId(patientId)
            .userId(UUID.randomUUID())
            .fullName("Test Patient")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .diabetesType(DiabetesType.TYPE_2)
            .diagnosisDate(LocalDate.of(2020, 1, 1))
            .heightCm(new BigDecimal("170"))
            .targetGlucoseMin(new BigDecimal("70"))
            .targetGlucoseMax(new BigDecimal("180"))
            .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
            .preferredGlucoseUnit(GlucoseUnit.MG_DL)
            .biologicalSex(BiologicalSex.NOT_SPECIFIED)
            .build();

    @BeforeEach
    void setUp() {
        detector = new NoMealAlertDetector(loadMealEntryPort, (key, args) -> "mensaje");
    }

    @Test
    @DisplayName("genera NO_MEAL_RECORDED cuando no hay comidas registradas hoy")
    void generatesNoMealAlertWhenEmpty() {
        when(loadMealEntryPort.findByPatientIdAndDate(any(), any())).thenReturn(List.of());

        List<Alert> alerts = detector.detect(patient, LocalDateTime.now());

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(Alert.AlertType.NO_MEAL_RECORDED);
    }

    @Test
    @DisplayName("no genera alerta cuando ya hay comidas registradas hoy")
    void noAlertWhenMealsExist() {
        MealEntry meal = MealEntry.builder()
                .mealId(UUID.randomUUID())
                .patientId(patientId)
                .mealType(MealType.BREAKFAST)
                .consumedAt(LocalDateTime.now())
                .build();
        when(loadMealEntryPort.findByPatientIdAndDate(any(), any())).thenReturn(List.of(meal));

        List<Alert> alerts = detector.detect(patient, LocalDateTime.now());

        assertThat(alerts).isEmpty();
    }
}
