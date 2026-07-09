package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMealEntryUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveMealEntryPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.exception.RateLimitExceededException;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterMealEntryUseCaseImpl")
class RegisterMealEntryUseCaseTest {

    @Mock
    private SaveMealEntryPort saveMealEntryPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private RegisterMealEntryUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("propaga la excepción cuando se excede el límite de registros, sin continuar el flujo")
        void propagatesExceptionWhenRateLimitExceeded() {
            doThrow(new RateLimitExceededException("límite excedido"))
                    .when(rateLimitService).checkMealLimit(patientId);

            RegisterMealEntryUseCase.Command command = new RegisterMealEntryUseCase.Command(
                    patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5), null, List.of(), null);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(RateLimitExceededException.class);

            verifyNoInteractions(loadPatientPort, saveMealEntryPort);
        }

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            RegisterMealEntryUseCase.Command command = new RegisterMealEntryUseCase.Command(
                    patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5), null, List.of(), null);

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(PatientNotFoundException.class);

            verifyNoInteractions(saveMealEntryPort);
        }

        @Test
        @DisplayName("registra la comida con todos sus items correctamente")
        void registersMealWithAllItems() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveMealEntryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            MealItem item1 = MealItem.create("Pan", BigDecimal.valueOf(50),
                    BigDecimal.valueOf(100), BigDecimal.valueOf(20), null, null, null);
            MealItem item2 = MealItem.create("Huevo", BigDecimal.valueOf(60),
                    BigDecimal.valueOf(80), BigDecimal.valueOf(1), null, null, null);

            RegisterMealEntryUseCase.Command command = new RegisterMealEntryUseCase.Command(
                    patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5),
                    "desayuno completo", List.of(item1, item2), null);

            MealEntry result = useCase.execute(command);

            assertThat(result.getItems()).containsExactly(item1, item2);
            assertThat(result.getMealType()).isEqualTo(MealType.BREAKFAST);
            assertThat(result.getNotes()).isEqualTo("desayuno completo");
        }

        @Test
        @DisplayName("honra el ID provisto por el cliente en vez de generar uno nuevo")
        void honorsClientProvidedId() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveMealEntryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            UUID clientMealId = UUID.randomUUID();

            RegisterMealEntryUseCase.Command command = new RegisterMealEntryUseCase.Command(
                    patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5), null, List.of(), clientMealId);

            MealEntry result = useCase.execute(command);

            assertThat(result.getMealId()).isEqualTo(clientMealId);
        }

        @Test
        @DisplayName("registra correctamente una comida sin items")
        void registersMealWithoutItems() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(saveMealEntryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterMealEntryUseCase.Command command = new RegisterMealEntryUseCase.Command(
                    patientId, MealType.SNACK, LocalDateTime.now().minusMinutes(5), null, List.of(), null);

            MealEntry result = useCase.execute(command);

            assertThat(result.getItems()).isEmpty();
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}