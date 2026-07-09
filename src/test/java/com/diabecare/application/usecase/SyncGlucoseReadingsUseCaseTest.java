package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncGlucoseReadingsUseCaseImpl")
class SyncGlucoseReadingsUseCaseTest {

    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;

    private SyncGlucoseReadingsUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @Test
    @DisplayName("delega al puerto con el cursor exacto cuando since viene informado")
    void delegatesToPortWithExactCursorWhenSinceIsProvided() {
        useCase = new SyncGlucoseReadingsUseCaseImpl(loadGlucoseReadingPort);
        LocalDateTime since = LocalDateTime.of(2026, 1, 1, 0, 0);
        GlucoseReading reading = GlucoseReading.create(
                patientId, BigDecimal.valueOf(120), GlucoseUnit.MG_DL,
                ReadingType.RANDOM, LocalDateTime.now().minusMinutes(5), null, null);
        when(loadGlucoseReadingPort.findByPatientIdUpdatedAfter(patientId, since))
                .thenReturn(List.of(reading));

        List<GlucoseReading> result = useCase.execute(patientId, since);

        assertThat(result).containsExactly(reading);
    }

    @Test
    @DisplayName("usa una época segura en vez de null cuando since está ausente")
    void usesSafeEpochInsteadOfNullWhenSinceIsAbsent() {
        useCase = new SyncGlucoseReadingsUseCaseImpl(loadGlucoseReadingPort);
        when(loadGlucoseReadingPort.findByPatientIdUpdatedAfter(any(), any())).thenReturn(List.of());

        useCase.execute(patientId, null);

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(loadGlucoseReadingPort).findByPatientIdUpdatedAfter(eq(patientId), sinceCaptor.capture());
        assertThat(sinceCaptor.getValue()).isNotNull();
        assertThat(sinceCaptor.getValue()).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
    }
}
