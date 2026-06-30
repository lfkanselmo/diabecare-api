package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UpdateInsulinProfileUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.DiabetesType;
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
@DisplayName("UpdateInsulinProfileUseCaseImpl")
class UpdateInsulinProfileUseCaseTest {

    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private SavePatientPort savePatientPort;
    @Mock
    private SaveAuditLogPort saveAuditLogPort;

    private UpdateInsulinProfileUseCaseImpl useCase;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateInsulinProfileUseCaseImpl(
                loadPatientPort, savePatientPort, saveAuditLogPort, new AuditService());
        lenient().when(savePatientPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            UUID patientId = UUID.randomUUID();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            UpdateInsulinProfileUseCase.Command command = new UpdateInsulinProfileUseCase.Command(
                    patientId, BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(120));

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(PatientNotFoundException.class);

            verifyNoInteractions(savePatientPort, saveAuditLogPort);
        }

        @Test
        @DisplayName("actualiza los 3 campos del perfil de insulina correctamente")
        void updatesAllThreeInsulinFields() {
            Patient patient = validPatient();
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdateInsulinProfileUseCase.Command command = new UpdateInsulinProfileUseCase.Command(
                    patient.getPatientId(), BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(120));

            Patient result = useCase.execute(command);

            assertThat(result.getInsulinSensitivityFactor()).isEqualByComparingTo(BigDecimal.valueOf(50));
            assertThat(result.getInsulinToCarbRatio()).isEqualByComparingTo(BigDecimal.valueOf(10));
            assertThat(result.getTargetGlucoseForCorrection()).isEqualByComparingTo(BigDecimal.valueOf(120));
        }

        @Test
        @DisplayName("registra auditoría de los 3 campos cuando todos cambian desde null")
        void auditsAllFieldsWhenChangingFromNull() {
            Patient patient = validPatient(); // sin perfil de insulina inicial (todo null)
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdateInsulinProfileUseCase.Command command = new UpdateInsulinProfileUseCase.Command(
                    patient.getPatientId(), BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(120));

            useCase.execute(command);

            verify(saveAuditLogPort).save(argThat(log ->
                    "insulinSensitivityFactor".equals(log.getFieldName())
                            && "null".equals(log.getOldValue()) && "50".equals(log.getNewValue())));
            verify(saveAuditLogPort).save(argThat(log ->
                    "insulinToCarbRatio".equals(log.getFieldName())
                            && "null".equals(log.getOldValue()) && "10".equals(log.getNewValue())));
            verify(saveAuditLogPort).save(argThat(log ->
                    "targetGlucoseForCorrection".equals(log.getFieldName())
                            && "null".equals(log.getOldValue()) && "120".equals(log.getNewValue())));
        }

        @Test
        @DisplayName("no registra auditoría cuando los valores no cambian")
        void doesNotAuditWhenValuesUnchanged() {
            Patient patient = validPatient();
            patient.updateInsulinProfile(BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(120));
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdateInsulinProfileUseCase.Command command = new UpdateInsulinProfileUseCase.Command(
                    patient.getPatientId(), BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(120));

            useCase.execute(command);

            verifyNoInteractions(saveAuditLogPort);
        }

        @Test
        @DisplayName("registra auditoría únicamente del campo que cambió")
        void auditsOnlyTheChangedField() {
            Patient patient = validPatient();
            patient.updateInsulinProfile(BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(120));
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdateInsulinProfileUseCase.Command command = new UpdateInsulinProfileUseCase.Command(
                    patient.getPatientId(), BigDecimal.valueOf(55), BigDecimal.valueOf(10), BigDecimal.valueOf(120));

            useCase.execute(command);

            verify(saveAuditLogPort, times(1)).save(any());
            verify(saveAuditLogPort).save(argThat(log ->
                    "insulinSensitivityFactor".equals(log.getFieldName())));
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Patient validPatient() {
        return Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}