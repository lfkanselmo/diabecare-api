package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMedicationsUseCaseImpl")
class GetMedicationsUseCaseTest {

    @Mock
    private LoadMedicationPort loadMedicationPort;

    @InjectMocks
    private GetMedicationsUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("getActiveByPatientId")
    class GetActiveByPatientId {

        @Test
        @DisplayName("retorna solo los medicamentos activos del paciente")
        void returnsOnlyActiveMedications() {
            Medication medication = Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                    DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), null);

            when(loadMedicationPort.findActiveByPatientId(patientId)).thenReturn(List.of(medication));

            List<Medication> result = useCase.getActiveByPatientId(patientId);

            assertThat(result).containsExactly(medication);
        }

        @Test
        @DisplayName("retorna lista vacía cuando el paciente no tiene medicamentos activos")
        void returnsEmptyListWhenNoActiveMedications() {
            when(loadMedicationPort.findActiveByPatientId(patientId)).thenReturn(List.of());

            List<Medication> result = useCase.getActiveByPatientId(patientId);

            assertThat(result).isEmpty();
        }
    }
}