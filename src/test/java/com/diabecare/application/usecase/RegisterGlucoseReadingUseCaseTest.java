package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveGlucoseReadingPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterGlucoseReadingUseCase")
class RegisterGlucoseReadingUseCaseTest {

    @Mock
    private LoadPatientPort loadPatientPort;

    @Mock
    private SaveGlucoseReadingPort saveGlucoseReadingPort;

    @InjectMocks
    private RegisterGlucoseReadingUseCaseImpl useCase;

    private UUID patientId;
    private Patient patient;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        patient   = buildPatient(patientId);
    }

    @Test
    @DisplayName("registra lectura correctamente cuando el paciente existe")
    void registersReadingSuccessfully() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(saveGlucoseReadingPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new RegisterGlucoseReadingUseCaseImpl.Command(
                patientId,
                new BigDecimal("120"),
                GlucoseUnit.MG_DL,
                ReadingType.FASTING,
                LocalDateTime.now().minusMinutes(10),
                null, null
        );

        GlucoseReading result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getValueInMgDl()).isEqualByComparingTo(new BigDecimal("120"));
        assertThat(result.getReadingType()).isEqualTo(ReadingType.FASTING);
        verify(saveGlucoseReadingPort, times(1)).save(any());
    }

    @Test
    @DisplayName("lanza excepción cuando el paciente no existe")
    void throwsExceptionWhenPatientNotFound() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

        var command = new RegisterGlucoseReadingUseCaseImpl.Command(
                patientId,
                new BigDecimal("120"),
                GlucoseUnit.MG_DL,
                ReadingType.FASTING,
                LocalDateTime.now().minusMinutes(10),
                null, null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(PatientNotFoundException.class);

        verify(saveGlucoseReadingPort, never()).save(any());
    }

    @Test
    @DisplayName("clasifica correctamente la glucosa como NORMAL")
    void classifiesNormalGlucose() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(saveGlucoseReadingPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new RegisterGlucoseReadingUseCaseImpl.Command(
                patientId, new BigDecimal("120"), GlucoseUnit.MG_DL,
                ReadingType.RANDOM, LocalDateTime.now().minusMinutes(5), null, null
        );

        GlucoseReading result = useCase.execute(command);
        assertThat(result.getStatus()).isEqualTo(GlucoseStatus.NORMAL);
    }

    @Test
    @DisplayName("clasifica correctamente la glucosa como HIGH")
    void classifiesHighGlucose() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(saveGlucoseReadingPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new RegisterGlucoseReadingUseCaseImpl.Command(
                patientId, new BigDecimal("250"), GlucoseUnit.MG_DL,
                ReadingType.RANDOM, LocalDateTime.now().minusMinutes(5), null, null
        );

        GlucoseReading result = useCase.execute(command);
        assertThat(result.getStatus()).isIn(GlucoseStatus.HIGH, GlucoseStatus.CRITICALLY_HIGH);
    }

    @Test
    @DisplayName("lanza excepción con fecha futura")
    void throwsExceptionWithFutureDate() {
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

        var command = new RegisterGlucoseReadingUseCaseImpl.Command(
                patientId, new BigDecimal("120"), GlucoseUnit.MG_DL,
                ReadingType.RANDOM, LocalDateTime.now().plusHours(1), null, null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(Exception.class);
    }

    private Patient buildPatient(UUID id) {
        return Patient.builder()
                .patientId(id)
                .userId(UUID.randomUUID())
                .fullName("Test Patient")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .diabetesType(DiabetesType.TYPE_2)
                .diagnosisDate(LocalDate.of(2020, 1, 1))
                .heightCm(new BigDecimal("170"))
                .targetGlucoseMin(new BigDecimal("70"))
                .targetGlucoseMax(new BigDecimal("180"))
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .preferredGlucoseUnit(GlucoseUnit.MG_DL)
                .biologicalSex(BiologicalSex.NOT_SPECIFIED)
                .build();
    }
}