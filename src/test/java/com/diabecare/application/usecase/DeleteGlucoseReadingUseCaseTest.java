package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.exception.GlucoseReadingNotFoundException;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
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
@DisplayName("DeleteGlucoseReadingUseCaseImpl")
class DeleteGlucoseReadingUseCaseTest {

    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;

    @InjectMocks
    private DeleteGlucoseReadingUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();
    private final UUID readingId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("elimina la lectura cuando pertenece al paciente que la solicita")
        void deletesReadingWhenOwnedByPatient() {
            GlucoseReading reading = readingOwnedBy(patientId);
            when(loadGlucoseReadingPort.findById(readingId)).thenReturn(Optional.of(reading));

            useCase.execute(readingId, patientId);

            verify(loadGlucoseReadingPort).deleteById(readingId);
        }

        @Test
        @DisplayName("lanza GlucoseReadingNotFoundException cuando la lectura no existe")
        void throwsWhenReadingNotFound() {
            when(loadGlucoseReadingPort.findById(readingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(readingId, patientId))
                    .isInstanceOf(GlucoseReadingNotFoundException.class)
                    .hasMessageContaining(readingId.toString());

            verify(loadGlucoseReadingPort, never()).deleteById(any());
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando la lectura pertenece a otro paciente")
        void throwsWhenReadingBelongsToAnotherPatient() {
            UUID otherPatientId = UUID.randomUUID();
            GlucoseReading reading = readingOwnedBy(otherPatientId);
            when(loadGlucoseReadingPort.findById(readingId)).thenReturn(Optional.of(reading));

            assertThatThrownBy(() -> useCase.execute(readingId, patientId))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);

            verify(loadGlucoseReadingPort, never()).deleteById(any());
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private GlucoseReading readingOwnedBy(UUID ownerId) {
        return GlucoseReading.builder()
                .readingId(readingId)
                .patientId(ownerId)
                .value(BigDecimal.valueOf(100))
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}