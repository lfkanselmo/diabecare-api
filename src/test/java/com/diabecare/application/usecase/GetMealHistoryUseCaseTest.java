package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMealHistoryUseCaseImpl")
class GetMealHistoryUseCaseTest {

    @Mock
    private LoadMealEntryPort loadMealEntryPort;

    @InjectMocks
    private GetMealHistoryUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("getHistory")
    class GetHistory {

        private final Pageable pageable = PageRequest.of(0, 20);

        @Test
        @DisplayName("retorna el historial de comidas del rango indicado")
        void returnsHistoryForGivenRange() {
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 7);
            MealEntry meal = MealEntry.create(
                    patientId, MealType.LUNCH, LocalDateTime.of(2026, 6, 3, 13, 0), null);

            when(loadMealEntryPort.findByPatientIdAndDateRange(patientId, from, to, pageable))
                    .thenReturn(new PageImpl<>(List.of(meal), pageable, 1));

            Page<MealEntry> result = useCase.getHistory(patientId, from, to, pageable);

            assertThat(result.getContent()).containsExactly(meal);
        }

        @Test
        @DisplayName("retorna página vacía cuando no hay comidas en el rango")
        void returnsEmptyListWhenNoMeals() {
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 7);

            when(loadMealEntryPort.findByPatientIdAndDateRange(patientId, from, to, pageable))
                    .thenReturn(Page.empty(pageable));

            Page<MealEntry> result = useCase.getHistory(patientId, from, to, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }
}