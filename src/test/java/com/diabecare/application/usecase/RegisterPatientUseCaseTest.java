package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterPatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterPatientUseCaseImpl")
class RegisterPatientUseCaseTest {

    @Mock
    private SavePatientPort savePatientPort;
    @Mock
    private LoadPatientPort loadPatientPort;

    @InjectMocks
    private RegisterPatientUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("registra el paciente con el sexo biológico especificado")
        void registersPatientWithSpecifiedBiologicalSex() {
            when(loadPatientPort.existsByUserId(userId)).thenReturn(false);
            when(savePatientPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterPatientUseCase.Command command = new RegisterPatientUseCase.Command(
                    userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1),
                    BigDecimal.valueOf(165), BiologicalSex.FEMALE);

            Patient result = useCase.execute(command);

            assertThat(result.getBiologicalSex()).isEqualTo(BiologicalSex.FEMALE);
            assertThat(result.getFullName()).isEqualTo("Ana García");
        }

        @Test
        @DisplayName("usa NOT_SPECIFIED cuando no se indica el sexo biológico")
        void usesNotSpecifiedWhenBiologicalSexIsNull() {
            when(loadPatientPort.existsByUserId(userId)).thenReturn(false);
            when(savePatientPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterPatientUseCase.Command command = new RegisterPatientUseCase.Command(
                    userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1),
                    BigDecimal.valueOf(165), null);

            Patient result = useCase.execute(command);

            assertThat(result.getBiologicalSex()).isEqualTo(BiologicalSex.NOT_SPECIFIED);
        }

        @Test
        @DisplayName("rechaza el registro cuando ya existe un perfil para el usuario")
        void rejectsWhenProfileAlreadyExists() {
            when(loadPatientPort.existsByUserId(userId)).thenReturn(true);

            RegisterPatientUseCase.Command command = new RegisterPatientUseCase.Command(
                    userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1),
                    BigDecimal.valueOf(165), BiologicalSex.FEMALE);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("Ya existe un perfil");

            verify(savePatientPort, never()).save(any());
        }

        @Test
        @DisplayName("persiste el paciente construido a través del puerto de guardado")
        void persistsPatientThroughSavePort() {
            when(loadPatientPort.existsByUserId(userId)).thenReturn(false);
            when(savePatientPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterPatientUseCase.Command command = new RegisterPatientUseCase.Command(
                    userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1),
                    BigDecimal.valueOf(165), BiologicalSex.FEMALE);

            useCase.execute(command);

            ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
            verify(savePatientPort).save(captor.capture());

            assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        }
    }
}