package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SaveMedicationPort;
import com.diabecare.domain.exception.InvalidMedicationException;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.domain.model.AuditLog;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.domain.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeactivateMedicationUseCaseImpl")
class DeactivateMedicationUseCaseTest {

    @Mock
    private LoadMedicationPort loadMedicationPort;
    @Mock
    private SaveMedicationPort saveMedicationPort;
    @Mock
    private SaveAuditLogPort saveAuditLogPort;

    private DeactivateMedicationUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();
    private final UUID medicationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new DeactivateMedicationUseCaseImpl(
                loadMedicationPort, saveMedicationPort, saveAuditLogPort, new AuditService());
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("desactiva el medicamento cuando pertenece al paciente que lo solicita")
        void deactivatesMedicationWhenOwnedByPatient() {
            Medication medication = medicationOwnedBy(patientId);
            when(loadMedicationPort.findById(medicationId)).thenReturn(Optional.of(medication));

            useCase.execute(medicationId, patientId);

            assertThat(medication.isActive()).isFalse();
            verify(saveMedicationPort).save(medication);
        }

        @Test
        @DisplayName("lanza InvalidMedicationException cuando el medicamento no existe")
        void throwsWhenMedicationNotFound() {
            when(loadMedicationPort.findById(medicationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(medicationId, patientId))
                    .isInstanceOf(InvalidMedicationException.class);

            verifyNoInteractions(saveMedicationPort, saveAuditLogPort);
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando el medicamento pertenece a otro paciente")
        void throwsWhenMedicationBelongsToAnotherPatient() {
            UUID otherPatientId = UUID.randomUUID();
            Medication medication = medicationOwnedBy(otherPatientId);
            when(loadMedicationPort.findById(medicationId)).thenReturn(Optional.of(medication));

            assertThatThrownBy(() -> useCase.execute(medicationId, patientId))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);

            verifyNoInteractions(saveMedicationPort, saveAuditLogPort);
        }

        @Test
        @DisplayName("registra un log de auditoría de eliminación antes de desactivar")
        void registersDeleteAuditLog() {
            Medication medication = medicationOwnedBy(patientId);
            when(loadMedicationPort.findById(medicationId)).thenReturn(Optional.of(medication));

            useCase.execute(medicationId, patientId);

            verify(saveAuditLogPort).save(argThat(log ->
                    log.getAction() == AuditLog.Action.DELETE
                            && "MEDICATION".equals(log.getEntityType())
                            && log.getEntityId().equals(medicationId)));
        }
    }


    private Medication medicationOwnedBy(UUID ownerId) {
        return Medication.create(
                ownerId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), null);
    }
}
