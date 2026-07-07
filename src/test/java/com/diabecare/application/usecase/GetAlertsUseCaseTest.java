package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.AlertDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAlertsUseCaseImpl")
class GetAlertsUseCaseTest {

    @Mock private LoadPatientPort loadPatientPort;
    @Mock private AlertDetector firstDetector;
    @Mock private AlertDetector secondDetector;

    private GetAlertsUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();
    private final Patient patient = buildPatient(patientId);

    @BeforeEach
    void setUp() {
        useCase = new GetAlertsUseCaseImpl(loadPatientPort, List.of(firstDetector, secondDetector));
    }

    @Test
    @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
    void throwsWhenPatientNotFound() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getAlerts(patientId))
                .isInstanceOf(PatientNotFoundException.class);

        verifyNoInteractions(firstDetector, secondDetector);
    }

    @Test
    @DisplayName("combina en orden las alertas devueltas por todos los detectores")
    void combinesAlertsFromAllDetectorsInOrder() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

        Alert alertA = buildAlert(Alert.AlertType.NO_GLUCOSE_RECORDED);
        Alert alertB = buildAlert(Alert.AlertType.POSITIVE_STREAK);
        Alert alertC = buildAlert(Alert.AlertType.NO_MEAL_RECORDED);

        when(firstDetector.detect(eq(patient), any())).thenReturn(List.of(alertA, alertB));
        when(secondDetector.detect(eq(patient), any())).thenReturn(List.of(alertC));

        List<Alert> alerts = useCase.getAlerts(patientId);

        assertThat(alerts).containsExactly(alertA, alertB, alertC);
    }

    @Test
    @DisplayName("retorna una lista vacía cuando ningún detector genera alertas")
    void returnsEmptyListWhenNoDetectorFires() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(firstDetector.detect(any(), any())).thenReturn(List.of());
        when(secondDetector.detect(any(), any())).thenReturn(List.of());

        assertThat(useCase.getAlerts(patientId)).isEmpty();
    }

    @Test
    @DisplayName("invoca a todos los detectores con el mismo paciente")
    void passesSamePatientToAllDetectors() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(firstDetector.detect(any(), any())).thenReturn(List.of());
        when(secondDetector.detect(any(), any())).thenReturn(List.of());

        useCase.getAlerts(patientId);

        verify(firstDetector).detect(eq(patient), any());
        verify(secondDetector).detect(eq(patient), any());
    }

    private Patient buildPatient(UUID id) {
        return Patient.builder()
                .patientId(id)
                .userId(UUID.randomUUID())
                .fullName("Test Patient")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .diabetesType(DiabetesType.TYPE_2)
                .diagnosisDate(LocalDate.of(2020, 1, 1))
                .heightCm(new BigDecimal("170"))
                .targetGlucoseMin(new BigDecimal("70"))
                .targetGlucoseMax(new BigDecimal("180"))
                .dailyCalorieGoal(2000)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .preferredGlucoseUnit(GlucoseUnit.MG_DL)
                .biologicalSex(BiologicalSex.NOT_SPECIFIED)
                .build();
    }

    private Alert buildAlert(Alert.AlertType type) {
        return Alert.builder()
                .type(type)
                .severity(Alert.Severity.INFO)
                .title("t")
                .message("m")
                .build();
    }
}
