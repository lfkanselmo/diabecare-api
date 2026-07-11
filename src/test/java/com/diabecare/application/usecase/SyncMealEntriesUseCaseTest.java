package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncMealEntriesUseCaseImpl")
class SyncMealEntriesUseCaseTest {

    @Mock
    private LoadMealEntryPort loadMealEntryPort;

    private SyncMealEntriesUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @Test
    @DisplayName("delega al puerto con el cursor exacto cuando since viene informado")
    void delegatesToPortWithExactCursorWhenSinceIsProvided() {
        useCase = new SyncMealEntriesUseCaseImpl(loadMealEntryPort);
        LocalDateTime since = LocalDateTime.of(2026, 1, 1, 0, 0);
        MealEntry meal = MealEntry.create(patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5), null);
        when(loadMealEntryPort.findByPatientIdUpdatedAfter(patientId, since)).thenReturn(List.of(meal));

        List<MealEntry> result = useCase.execute(patientId, since);

        assertThat(result).containsExactly(meal);
    }

    @Test
    @DisplayName("usa una época segura en vez de null cuando since está ausente")
    void usesSafeEpochInsteadOfNullWhenSinceIsAbsent() {
        useCase = new SyncMealEntriesUseCaseImpl(loadMealEntryPort);
        when(loadMealEntryPort.findByPatientIdUpdatedAfter(any(), any())).thenReturn(List.of());

        useCase.execute(patientId, null);

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(loadMealEntryPort).findByPatientIdUpdatedAfter(eq(patientId), sinceCaptor.capture());
        assertThat(sinceCaptor.getValue()).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
    }
}
