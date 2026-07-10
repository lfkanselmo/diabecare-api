package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMenstrualCycleUseCase;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveMenstrualCyclePort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.exception.OpenCycleConflictException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.MenstrualCycle;
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
@DisplayName("RegisterMenstrualCycleUseCaseImpl")
class RegisterMenstrualCycleUseCaseTest {

    @Mock
    private SaveMenstrualCyclePort saveMenstrualCyclePort;
    @Mock
    private LoadMenstrualCyclePort loadMenstrualCyclePort;
    @Mock
    private LoadPatientPort loadPatientPort;

    @InjectMocks
    private RegisterMenstrualCycleUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            RegisterMenstrualCycleUseCase.Command command = new RegisterMenstrualCycleUseCase.Command(
                    patientId, LocalDate.of(2026, 6, 1), null);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(PatientNotFoundException.class);

            verifyNoInteractions(saveMenstrualCyclePort);
        }

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando el paciente no es de sexo femenino")
        void throwsWhenPatientNotFemale() {
            Patient malePatient = femalePatient();
            malePatient.updateBiologicalSex(BiologicalSex.MALE);
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(malePatient));

            RegisterMenstrualCycleUseCase.Command command = new RegisterMenstrualCycleUseCase.Command(
                    patientId, LocalDate.of(2026, 6, 1), null);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("sexo femenino");

            verifyNoInteractions(loadMenstrualCyclePort, saveMenstrualCyclePort);
        }

        @Test
        @DisplayName("lanza OpenCycleConflictException cuando ya hay un ciclo en curso")
        void throwsWhenOpenCycleAlreadyExists() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(femalePatient()));

            MenstrualCycle ongoing = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.of(2026, 6, 1), null);
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId))
                    .thenReturn(Optional.of(ongoing));

            RegisterMenstrualCycleUseCase.Command command = new RegisterMenstrualCycleUseCase.Command(
                    patientId, LocalDate.of(2026, 6, 15), null);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(OpenCycleConflictException.class);

            verifyNoInteractions(saveMenstrualCyclePort);
        }

        @Test
        @DisplayName("registra el ciclo correctamente cuando el último ciclo ya está cerrado")
        void registersCycleWhenLastCycleIsFinished() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(femalePatient()));

            MenstrualCycle finished = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.of(2026, 5, 1), null);
            finished.finish(LocalDate.of(2026, 5, 6));
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId))
                    .thenReturn(Optional.of(finished));
            when(saveMenstrualCyclePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterMenstrualCycleUseCase.Command command = new RegisterMenstrualCycleUseCase.Command(
                    patientId, LocalDate.of(2026, 6, 1), "ciclo nuevo");

            MenstrualCycle result = useCase.execute(command);

            assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(result.isOngoing()).isTrue();
        }

        @Test
        @DisplayName("registra el ciclo correctamente cuando es el primer ciclo del paciente")
        void registersCycleWhenItIsTheFirstOne() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(femalePatient()));
            when(loadMenstrualCyclePort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());
            when(saveMenstrualCyclePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterMenstrualCycleUseCase.Command command = new RegisterMenstrualCycleUseCase.Command(
                    patientId, LocalDate.of(2026, 6, 1), null);

            MenstrualCycle result = useCase.execute(command);

            assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        }
    }


    private Patient femalePatient() {
        Patient patient = Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
        patient.updateBiologicalSex(BiologicalSex.FEMALE);
        return patient;
    }
}
