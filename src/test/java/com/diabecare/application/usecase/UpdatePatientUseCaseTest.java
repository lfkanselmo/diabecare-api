package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UpdatePatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.AuditLog;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePatientUseCaseImpl")
class UpdatePatientUseCaseTest {

    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private SavePatientPort savePatientPort;
    @Mock
    private SaveAuditLogPort saveAuditLogPort;

    private UpdatePatientUseCaseImpl useCase;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdatePatientUseCaseImpl(
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

            UpdatePatientUseCase.Command command = commandFor(patientId);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(PatientNotFoundException.class);

            verifyNoInteractions(savePatientPort, saveAuditLogPort);
        }

        @Test
        @DisplayName("actualiza el rango de glucosa, meta calórica, nivel de actividad y unidad preferida")
        void updatesAllEditableFields() {
            Patient patient = validPatient();
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdatePatientUseCase.Command command = new UpdatePatientUseCase.Command(
                    patient.getPatientId(), BigDecimal.valueOf(170),
                    BigDecimal.valueOf(80), BigDecimal.valueOf(160),
                    2000, ActivityLevel.VERY_ACTIVE, GlucoseUnit.MMOL_L);

            Patient result = useCase.execute(command);

            assertThat(result.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(170));
            assertThat(result.getTargetGlucoseMin()).isEqualByComparingTo(BigDecimal.valueOf(80));
            assertThat(result.getTargetGlucoseMax()).isEqualByComparingTo(BigDecimal.valueOf(160));
            assertThat(result.getDailyCalorieGoal()).isEqualTo(2000);
            assertThat(result.getActivityLevel()).isEqualTo(ActivityLevel.VERY_ACTIVE);
            assertThat(result.getPreferredGlucoseUnit()).isEqualTo(GlucoseUnit.MMOL_L);
        }

        @Test
        @DisplayName("no actualiza la talla cuando el comando la trae en null")
        void doesNotUpdateHeightWhenNull() {
            Patient patient = validPatient();
            BigDecimal originalHeight = patient.getHeightCm();
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdatePatientUseCase.Command command = new UpdatePatientUseCase.Command(
                    patient.getPatientId(), null,
                    BigDecimal.valueOf(70), BigDecimal.valueOf(180),
                    null, null, null);

            Patient result = useCase.execute(command);

            assertThat(result.getHeightCm()).isEqualByComparingTo(originalHeight);
        }

        @Test
        @DisplayName("registra auditoría de talla solo cuando el comando incluye un valor distinto al actual")
        void auditsHeightOnlyWhenChanged() {
            Patient patient = validPatient(); // talla inicial 165
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdatePatientUseCase.Command command = new UpdatePatientUseCase.Command(
                    patient.getPatientId(), BigDecimal.valueOf(170),
                    BigDecimal.valueOf(70), BigDecimal.valueOf(180),
                    null, null, null);

            useCase.execute(command);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(saveAuditLogPort, atLeastOnce()).save(captor.capture());

            assertThat(captor.getAllValues())
                    .anySatisfy(log -> {
                        assertThat(log.getFieldName()).isEqualTo("heightCm");
                        assertThat(log.getOldValue()).isEqualTo("165");
                        assertThat(log.getNewValue()).isEqualTo("170");
                    });
        }

        @Test
        @DisplayName("no registra auditoría de talla cuando el nuevo valor es igual al actual")
        void doesNotAuditHeightWhenUnchanged() {
            Patient patient = validPatient(); // talla inicial 165
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdatePatientUseCase.Command command = new UpdatePatientUseCase.Command(
                    patient.getPatientId(), BigDecimal.valueOf(165),
                    BigDecimal.valueOf(70), BigDecimal.valueOf(180),
                    null, null, null);

            useCase.execute(command);

            verify(saveAuditLogPort, never()).save(argThat(log ->
                    "heightCm".equals(log.getFieldName())));
        }

        @Test
        @DisplayName("registra auditoría del rango de glucosa cuando cambia")
        void auditsGlucoseTargetWhenChanged() {
            Patient patient = validPatient(); // rango inicial 70-180
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdatePatientUseCase.Command command = new UpdatePatientUseCase.Command(
                    patient.getPatientId(), null,
                    BigDecimal.valueOf(80), BigDecimal.valueOf(160),
                    null, null, null);

            useCase.execute(command);

            verify(saveAuditLogPort).save(argThat(log ->
                    "targetGlucoseRange".equals(log.getFieldName())
                            && "70-180".equals(log.getOldValue())
                            && "80-160".equals(log.getNewValue())));
        }

        @Test
        @DisplayName("no registra ninguna auditoría cuando ningún campo cambia")
        void doesNotAuditAnythingWhenNothingChanges() {
            Patient patient = validPatient(); // rango 70-180, sin meta calórica/nivel/unidad explícitos aún
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdatePatientUseCase.Command command = new UpdatePatientUseCase.Command(
                    patient.getPatientId(), null,
                    BigDecimal.valueOf(70), BigDecimal.valueOf(180),
                    null, null, null);

            useCase.execute(command);

            verifyNoInteractions(saveAuditLogPort);
        }

        @Test
        @DisplayName("no registra auditoría de meta calórica, nivel de actividad o unidad cuando vienen en null")
        void doesNotAuditOptionalFieldsWhenNull() {
            Patient patient = validPatient();
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            UpdatePatientUseCase.Command command = new UpdatePatientUseCase.Command(
                    patient.getPatientId(), null,
                    BigDecimal.valueOf(70), BigDecimal.valueOf(180),
                    null, null, null);

            useCase.execute(command);

            verify(saveAuditLogPort, never()).save(argThat(log ->
                    "dailyCalorieGoal".equals(log.getFieldName())
                            || "activityLevel".equals(log.getFieldName())
                            || "preferredGlucoseUnit".equals(log.getFieldName())));
        }
    }


    private UpdatePatientUseCase.Command commandFor(UUID patientId) {
        return new UpdatePatientUseCase.Command(
                patientId, BigDecimal.valueOf(170),
                BigDecimal.valueOf(70), BigDecimal.valueOf(180),
                null, null, null);
    }

    private Patient validPatient() {
        return Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}
