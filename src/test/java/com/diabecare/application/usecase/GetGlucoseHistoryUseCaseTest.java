package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetGlucoseHistoryUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealType;
import com.diabecare.domain.model.ReadingType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetGlucoseHistoryUseCaseImpl")
class GetGlucoseHistoryUseCaseTest {

    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock
    private LoadMealEntryPort loadMealEntryPort;

    @InjectMocks
    private GetGlucoseHistoryUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("getByPatientAndDateRange")
    class GetByPatientAndDateRange {

        private final Pageable pageable = PageRequest.of(0, 50);

        @Test
        @DisplayName("combina lecturas y comidas del mismo rango en el resultado")
        void combinesReadingsAndMealsFromSameRange() {
            LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
            LocalDateTime to = LocalDateTime.of(2026, 6, 7, 23, 59);

            GlucoseReading reading = readingAt(LocalDateTime.of(2026, 6, 3, 8, 0));
            MealEntry meal = mealAt(LocalDateTime.of(2026, 6, 3, 8, 30));

            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to, pageable))
                    .thenReturn(new PageImpl<>(List.of(reading), pageable, 1));
            when(loadMealEntryPort.findByPatientIdAndDateRange(
                    patientId, from.toLocalDate(), to.toLocalDate()))
                    .thenReturn(List.of(meal));

            GetGlucoseHistoryUseCase.Result result =
                    useCase.getByPatientAndDateRange(patientId, from, to, pageable);

            assertThat(result.readings().getContent()).containsExactly(reading);
            assertThat(result.mealEntries()).containsExactly(meal);
        }

        @Test
        @DisplayName("convierte correctamente las fechas LocalDateTime a LocalDate para la consulta de comidas")
        void convertsDateTimesToDatesForMealQuery() {
            LocalDateTime from = LocalDateTime.of(2026, 6, 1, 14, 30);
            LocalDateTime to = LocalDateTime.of(2026, 6, 7, 9, 15);

            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any(), any()))
                    .thenReturn(Page.empty(pageable));
            when(loadMealEntryPort.findByPatientIdAndDateRange(any(), any(), any()))
                    .thenReturn(List.of());

            useCase.getByPatientAndDateRange(patientId, from, to, pageable);

            verify(loadMealEntryPort).findByPatientIdAndDateRange(
                    patientId, from.toLocalDate(), to.toLocalDate());
        }

        @Test
        @DisplayName("retorna listas vacías cuando no hay datos en el rango")
        void returnsEmptyListsWhenNoData() {
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();

            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any(), any()))
                    .thenReturn(Page.empty(pageable));
            when(loadMealEntryPort.findByPatientIdAndDateRange(any(), any(), any()))
                    .thenReturn(List.of());

            GetGlucoseHistoryUseCase.Result result =
                    useCase.getByPatientAndDateRange(patientId, from, to, pageable);

            assertThat(result.readings().getContent()).isEmpty();
            assertThat(result.mealEntries()).isEmpty();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private GlucoseReading readingAt(LocalDateTime measuredAt) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(patientId)
                .value(BigDecimal.valueOf(100))
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(measuredAt)
                .build();
    }

    private MealEntry mealAt(LocalDateTime consumedAt) {
        return MealEntry.create(patientId, MealType.BREAKFAST, consumedAt, null);
    }
}