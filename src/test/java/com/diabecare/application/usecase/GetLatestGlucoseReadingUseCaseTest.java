package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLatestGlucoseReadingUseCaseImpl")
class GetLatestGlucoseReadingUseCaseTest {

    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;

    @InjectMocks
    private GetLatestGlucoseReadingUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("getLatest")
    class GetLatest {

        @Test
        @DisplayName("retorna la lectura más reciente cuando existe")
        void returnsLatestReadingWhenExists() {
            GlucoseReading reading = GlucoseReading.builder()
                    .readingId(UUID.randomUUID())
                    .patientId(patientId)
                    .value(BigDecimal.valueOf(110))
                    .unit(GlucoseUnit.MG_DL)
                    .readingType(ReadingType.RANDOM)
                    .measuredAt(LocalDateTime.now())
                    .build();

            when(loadGlucoseReadingPort.findLatestByPatientId(patientId))
                    .thenReturn(Optional.of(reading));

            Optional<GlucoseReading> result = useCase.getLatest(patientId);

            assertThat(result).contains(reading);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el paciente no tiene lecturas")
        void returnsEmptyWhenNoReadingsExist() {
            when(loadGlucoseReadingPort.findLatestByPatientId(patientId))
                    .thenReturn(Optional.empty());

            Optional<GlucoseReading> result = useCase.getLatest(patientId);

            assertThat(result).isEmpty();
        }
    }
}