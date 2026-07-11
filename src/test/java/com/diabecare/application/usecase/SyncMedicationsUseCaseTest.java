package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncMedicationsUseCaseImpl")
class SyncMedicationsUseCaseTest {

    @Mock
    private LoadMedicationPort loadMedicationPort;

    private SyncMedicationsUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @Test
    @DisplayName("delega al puerto con el cursor exacto cuando since viene informado")
    void delegatesToPortWithExactCursorWhenSinceIsProvided() {
        useCase = new SyncMedicationsUseCaseImpl(loadMedicationPort);
        LocalDateTime since = LocalDateTime.of(2026, 1, 1, 0, 0);
        Medication medication = Medication.create(
                patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), null);
        when(loadMedicationPort.findByPatientIdUpdatedAfter(patientId, since)).thenReturn(List.of(medication));

        List<Medication> result = useCase.execute(patientId, since);

        assertThat(result).containsExactly(medication);
    }

    @Test
    @DisplayName("usa una época segura en vez de null cuando since está ausente")
    void usesSafeEpochInsteadOfNullWhenSinceIsAbsent() {
        useCase = new SyncMedicationsUseCaseImpl(loadMedicationPort);
        when(loadMedicationPort.findByPatientIdUpdatedAfter(any(), any())).thenReturn(List.of());

        useCase.execute(patientId, null);

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(loadMedicationPort).findByPatientIdUpdatedAfter(eq(patientId), sinceCaptor.capture());
        assertThat(sinceCaptor.getValue()).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
    }
}
