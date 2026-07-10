package com.diabecare.application.usecase;

import com.diabecare.application.port.in.CalculateInsulinDoseUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
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
@DisplayName("CalculateInsulinDoseUseCaseImpl")
class CalculateInsulinDoseUseCaseTest {

    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private MessageResolverPort messages;

    private CalculateInsulinDoseUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CalculateInsulinDoseUseCaseImpl(loadPatientPort, messages);
        lenient().when(messages.resolve(any(), any())).thenReturn("texto");
        lenient().when(messages.resolve(any(), any(), any())).thenReturn("texto");
        lenient().when(messages.resolve(any())).thenReturn("texto");
    }

    @Nested
    @DisplayName("calculate")
    class Calculate {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(250), null, false);

            assertThatThrownBy(() -> useCase.calculate(command))
                    .isInstanceOf(PatientNotFoundException.class);
        }

        @Test
        @DisplayName("lanza InvalidPatientDataException cuando el perfil de insulina no está configurado")
        void throwsWhenInsulinProfileNotConfigured() {
            Patient patient = validPatient(); // sin perfil de insulina
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(250), null, false);

            assertThatThrownBy(() -> useCase.calculate(command))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("perfil de insulina");
        }

        @Test
        @DisplayName("calcula solo la dosis de corrección cuando no es antes de una comida")
        void calculatesOnlyCorrectionDoseWhenNotBeforeMeal() {
            Patient patient = patientWithInsulinProfile();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(250), null, false);

            CalculateInsulinDoseUseCase.Result result = useCase.calculate(command);

            assertThat(result.correctionDose()).isEqualByComparingTo(BigDecimal.valueOf(2.6).setScale(1));
            assertThat(result.mealDose()).isEqualByComparingTo(BigDecimal.ZERO.setScale(1));
            assertThat(result.totalDose()).isEqualByComparingTo(BigDecimal.valueOf(2.6).setScale(1));
        }

        @Test
        @DisplayName("calcula la dosis de corrección más la dosis de comida cuando es antes de comer")
        void calculatesCorrectionPlusMealDoseWhenBeforeMeal() {
            Patient patient = patientWithInsulinProfile();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(250), BigDecimal.valueOf(60), true);

            CalculateInsulinDoseUseCase.Result result = useCase.calculate(command);

            assertThat(result.correctionDose()).isEqualByComparingTo(BigDecimal.valueOf(2.6).setScale(1));
            assertThat(result.mealDose()).isEqualByComparingTo(BigDecimal.valueOf(6).setScale(1));
            assertThat(result.totalDose()).isEqualByComparingTo(BigDecimal.valueOf(8.6).setScale(1));
        }

        @Test
        @DisplayName("trunca la dosis de corrección a cero cuando la glucosa actual está por debajo del objetivo")
        void truncatesCorrectionToZeroWhenBelowTarget() {
            Patient patient = patientWithInsulinProfile();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(100), null, false);

            CalculateInsulinDoseUseCase.Result result = useCase.calculate(command);

            assertThat(result.correctionDose()).isEqualByComparingTo(BigDecimal.ZERO.setScale(1));
        }

        @Test
        @DisplayName("no calcula dosis de comida cuando beforeMeal es true pero no se especifican carbohidratos")
        void doesNotCalculateMealDoseWhenCarbsNotProvided() {
            Patient patient = patientWithInsulinProfile();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(250), null, true);

            CalculateInsulinDoseUseCase.Result result = useCase.calculate(command);

            assertThat(result.mealDose()).isEqualByComparingTo(BigDecimal.ZERO.setScale(1));
        }

        @Test
        @DisplayName("no calcula dosis de comida cuando beforeMeal es false, aunque haya carbohidratos")
        void doesNotCalculateMealDoseWhenNotBeforeMeal() {
            Patient patient = patientWithInsulinProfile();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(120), BigDecimal.valueOf(60), false);

            CalculateInsulinDoseUseCase.Result result = useCase.calculate(command);

            assertThat(result.mealDose()).isEqualByComparingTo(BigDecimal.ZERO.setScale(1));
        }

        @Test
        @DisplayName("incluye una explicación no vacía en el resultado")
        void includesNonEmptyExplanation() {
            Patient patient = patientWithInsulinProfile();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(250), BigDecimal.valueOf(60), true);

            CalculateInsulinDoseUseCase.Result result = useCase.calculate(command);

            assertThat(result.explanation()).isNotBlank();
        }

        @Test
        @DisplayName("incluye un disclaimer no vacío resuelto vía MessageResolverPort")
        void includesNonBlankDisclaimer() {
            Patient patient = patientWithInsulinProfile();
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
            when(messages.resolve("insulin.disclaimer")).thenReturn(
                    "No es una recomendación clínica automática.");

            CalculateInsulinDoseUseCase.Command command = new CalculateInsulinDoseUseCase.Command(
                    patientId, BigDecimal.valueOf(250), BigDecimal.valueOf(60), true);

            CalculateInsulinDoseUseCase.Result result = useCase.calculate(command);

            assertThat(result.disclaimer()).isEqualTo("No es una recomendación clínica automática.");
        }
    }


    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }

    private Patient patientWithInsulinProfile() {
        Patient patient = validPatient();
        patient.updateInsulinProfile(
                BigDecimal.valueOf(50),  // factor de sensibilidad
                BigDecimal.valueOf(10),  // ratio insulina:carbohidratos
                BigDecimal.valueOf(120)  // glucosa objetivo de corrección
        );
        return patient;
    }
}
