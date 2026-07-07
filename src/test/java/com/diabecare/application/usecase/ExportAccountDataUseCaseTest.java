package com.diabecare.application.usecase;

import com.diabecare.application.port.out.*;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.AccountExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportAccountDataUseCaseImpl")
class ExportAccountDataUseCaseTest {

    @Mock private LoadUserPort loadUserPort;
    @Mock private LoadPatientPort loadPatientPort;
    @Mock private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock private LoadMealEntryPort loadMealEntryPort;
    @Mock private LoadVitalSignPort loadVitalSignPort;
    @Mock private LoadMedicationPort loadMedicationPort;
    @Mock private LoadExerciseLogPort loadExerciseLogPort;
    @Mock private LoadMenstrualCyclePort loadMenstrualCyclePort;
    @Mock private LoadCaregiverLinkPort loadCaregiverLinkPort;

    private ExportAccountDataUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ExportAccountDataUseCaseImpl(
                loadUserPort, loadPatientPort, loadGlucoseReadingPort, loadMealEntryPort,
                loadVitalSignPort, loadMedicationPort, loadExerciseLogPort, loadMenstrualCyclePort,
                loadCaregiverLinkPort, new AccountExportService());
    }

    @Nested
    @DisplayName("exportAsJson")
    class ExportAsJson {

        @Test
        @DisplayName("incluye el email, el perfil de paciente y todas las categorias de datos en el JSON")
        void includesEmailPatientProfileAndAllDataCategoriesInJson() {
            User user = User.builder()
                    .id(userId).email("ana@example.com").role("PATIENT")
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                    .termsAcceptedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                    .termsVersion("2026-07")
                    .build();
            Patient patient = validPatient();

            when(loadUserPort.findById(userId)).thenReturn(Optional.of(user));
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.of(patient));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(List.of());
            when(loadMealEntryPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(List.of());
            when(loadVitalSignPort.findByPatientId(patientId)).thenReturn(List.of());
            when(loadMedicationPort.findAllByPatientId(patientId)).thenReturn(List.of());
            when(loadExerciseLogPort.findByPatientIdAndDateRange(any(), any(), any())).thenReturn(List.of());
            when(loadMenstrualCyclePort.findByPatientId(patientId)).thenReturn(List.of());
            when(loadCaregiverLinkPort.findActiveByPatientId(patientId)).thenReturn(List.of());
            when(loadCaregiverLinkPort.findActiveByCaregiverUserId(userId)).thenReturn(List.of());

            byte[] result = useCase.exportAsJson(userId);
            String json = new String(result, StandardCharsets.UTF_8);

            assertThat(json).contains("ana@example.com");
            assertThat(json).contains("Ana García");
            assertThat(json).contains("2026-07");
        }

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el usuario no tiene perfil de paciente")
        void throwsWhenUserHasNoPatientProfile() {
            User user = User.builder().id(userId).email("ana@example.com").role("PATIENT").build();
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(user));
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.exportAsJson(userId))
                    .isInstanceOf(PatientNotFoundException.class);
        }
    }

    private Patient validPatient() {
        Patient created = Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
        return Patient.builder()
                .patientId(patientId)
                .userId(userId)
                .fullName(created.getFullName())
                .dateOfBirth(created.getDateOfBirth())
                .diabetesType(created.getDiabetesType())
                .diagnosisDate(created.getDiagnosisDate())
                .heightCm(created.getHeightCm())
                .build();
    }
}
