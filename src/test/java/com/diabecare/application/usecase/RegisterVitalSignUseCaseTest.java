package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterVitalSignUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveVitalSignPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.model.VitalSign;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterVitalSignUseCaseImpl")
class RegisterVitalSignUseCaseTest {

    @Mock
    private SaveVitalSignPort saveVitalSignPort;
    @Mock
    private LoadPatientPort loadPatientPort;

    @InjectMocks
    private RegisterVitalSignUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            RegisterVitalSignUseCase.Command command = new RegisterVitalSignUseCase.Command(
                    patientId, BigDecimal.valueOf(70), BigDecimal.valueOf(170),
                    120, 80, 70, BigDecimal.valueOf(6.5), LocalDateTime.now().minusMinutes(5), null);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(PatientNotFoundException.class);

            verifyNoInteractions(saveVitalSignPort);
        }

        @Test
        @DisplayName("registra el signo vital con todos los campos correctamente")
        void registersVitalSignWithAllFields() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveVitalSignPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterVitalSignUseCase.Command command = new RegisterVitalSignUseCase.Command(
                    patientId, BigDecimal.valueOf(70), BigDecimal.valueOf(170),
                    120, 80, 70, BigDecimal.valueOf(6.5), LocalDateTime.now().minusMinutes(5), "control rutinario");

            VitalSign result = useCase.execute(command);

            assertThat(result.getWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(70));
            assertThat(result.getSystolicBp()).isEqualTo(120);
            assertThat(result.getNotes()).isEqualTo("control rutinario");
        }

        @Test
        @DisplayName("registra correctamente un signo vital con solo algunos campos opcionales")
        void registersVitalSignWithPartialFields() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveVitalSignPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterVitalSignUseCase.Command command = new RegisterVitalSignUseCase.Command(
                    patientId, BigDecimal.valueOf(70), null, null, null, null, null,
                    LocalDateTime.now().minusMinutes(5), null);

            VitalSign result = useCase.execute(command);

            assertThat(result.getWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(70));
            assertThat(result.getSystolicBp()).isNull();
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}