package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterExerciseUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveExerciseLogPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.exception.RateLimitExceededException;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.RateLimitService;
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
@DisplayName("RegisterExerciseUseCaseImpl")
class RegisterExerciseUseCaseTest {

    @Mock
    private SaveExerciseLogPort saveExerciseLogPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private RegisterExerciseUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("propaga la excepción cuando se excede el límite de registros, sin continuar el flujo")
        void propagatesExceptionWhenRateLimitExceeded() {
            doThrow(new RateLimitExceededException("límite excedido"))
                    .when(rateLimitService).checkExerciseLimit(patientId);

            RegisterExerciseUseCase.Command command = new RegisterExerciseUseCase.Command(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    30, null, LocalDateTime.now().minusMinutes(5), null);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(RateLimitExceededException.class);

            verifyNoInteractions(loadPatientPort, saveExerciseLogPort);
        }

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            RegisterExerciseUseCase.Command command = new RegisterExerciseUseCase.Command(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    30, null, LocalDateTime.now().minusMinutes(5), null);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(PatientNotFoundException.class);

            verifyNoInteractions(saveExerciseLogPort);
        }

        @Test
        @DisplayName("registra el ejercicio con calorías estimadas automáticamente")
        void registersExerciseWithEstimatedCalories() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveExerciseLogPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterExerciseUseCase.Command command = new RegisterExerciseUseCase.Command(
                    patientId, ExerciseType.WALKING, ExerciseIntensity.MODERATE,
                    60, "caminata en el parque", LocalDateTime.now().minusMinutes(5), null);

            ExerciseLog result = useCase.execute(command);

            assertThat(result.getExerciseType()).isEqualTo(ExerciseType.WALKING);
            assertThat(result.getDurationMinutes()).isEqualTo(60);
            assertThat(result.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(245));
        }

        @Test
        @DisplayName("registra el ejercicio usando el override de calorías cuando se especifica")
        void registersExerciseUsingCaloriesOverride() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveExerciseLogPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterExerciseUseCase.Command command = new RegisterExerciseUseCase.Command(
                    patientId, ExerciseType.RUNNING, ExerciseIntensity.HIGH,
                    45, null, LocalDateTime.now().minusMinutes(5), BigDecimal.valueOf(500));

            ExerciseLog result = useCase.execute(command);

            assertThat(result.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(500));
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}