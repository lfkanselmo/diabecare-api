package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetPatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPatientUseCaseImpl")
class GetPatientUseCaseTest {

    @Mock
    private LoadPatientPort loadPatientPort;

    @InjectMocks
    private GetPatientUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("retorna el paciente envuelto en Result cuando existe")
        void returnsPatientWrappedInResultWhenExists() {
            Patient patient = validPatient();
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            GetPatientUseCase.Result result = useCase.getById(patient.getPatientId());

            assertThat(result.patient()).isEqualTo(patient);
        }

        @Test
        @DisplayName("lanza PatientNotFoundException cuando no existe")
        void throwsWhenNotFound() {
            UUID patientId = UUID.randomUUID();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getById(patientId))
                    .isInstanceOf(PatientNotFoundException.class)
                    .hasMessageContaining(patientId.toString());
        }
    }

    @Nested
    @DisplayName("getByUserId")
    class GetByUserId {

        @Test
        @DisplayName("retorna el paciente envuelto en Result cuando existe")
        void returnsPatientWrappedInResultWhenExists() {
            Patient patient = validPatient();
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.of(patient));

            GetPatientUseCase.Result result = useCase.getByUserId(userId);

            assertThat(result.patient()).isEqualTo(patient);
        }

        @Test
        @DisplayName("lanza PatientNotFoundException cuando no existe")
        void throwsWhenNotFound() {
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getByUserId(userId))
                    .isInstanceOf(PatientNotFoundException.class)
                    .hasMessageContaining(userId.toString());
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Patient validPatient() {
        return Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}