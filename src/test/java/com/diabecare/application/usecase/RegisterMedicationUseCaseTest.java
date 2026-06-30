package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMedicationUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SaveMedicationPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.AuditLog;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.domain.model.Patient;
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
@DisplayName("RegisterMedicationUseCaseImpl")
class RegisterMedicationUseCaseTest {

    @Mock
    private SaveMedicationPort saveMedicationPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private SaveAuditLogPort saveAuditLogPort;

    private RegisterMedicationUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RegisterMedicationUseCaseImpl(
                saveMedicationPort, loadPatientPort, saveAuditLogPort, new AuditService());
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            RegisterMedicationUseCase.Command command = validCommand();

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(PatientNotFoundException.class);

            verifyNoInteractions(saveMedicationPort, saveAuditLogPort);
        }

        @Test
        @DisplayName("registra el medicamento correctamente")
        void registersMedicationCorrectly() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveMedicationPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Medication result = useCase.execute(validCommand());

            assertThat(result.getName()).isEqualTo("Metformina");
            assertThat(result.getDose()).isEqualByComparingTo(BigDecimal.valueOf(500));
            assertThat(result.isActive()).isTrue();
        }

        @Test
        @DisplayName("registra un log de auditoría de creación con el id del medicamento guardado")
        void registersCreateAuditLog() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveMedicationPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Medication result = useCase.execute(validCommand());

            verify(saveAuditLogPort).save(argThat(log ->
                    log.getAction() == AuditLog.Action.CREATE
                            && "MEDICATION".equals(log.getEntityType())
                            && log.getEntityId().equals(result.getMedicationId())));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private RegisterMedicationUseCase.Command validCommand() {
        return new RegisterMedicationUseCase.Command(
                patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), "con comidas");
    }

    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}