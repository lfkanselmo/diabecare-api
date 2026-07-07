package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.AgpHourlyBucket;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.domain.service.AgpProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAgpProfileUseCaseImpl")
class GetAgpProfileUseCaseTest {

    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;

    private GetAgpProfileUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetAgpProfileUseCaseImpl(loadGlucoseReadingPort, new AgpProfileService());
    }

    @Test
    @DisplayName("delega en el puerto de lecturas y construye el perfil de 24 horas")
    void delegatesToReadingPortAndBuildsTwentyFourHourProfile() {
        LocalDateTime from = LocalDateTime.now().minusDays(14);
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime measuredAt = LocalDateTime.of(2026, 1, 1, 8, 0);

        GlucoseReading reading = GlucoseReading.create(
                patientId, BigDecimal.valueOf(100), GlucoseUnit.MG_DL,
                ReadingType.RANDOM, measuredAt, null, null);

        when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to))
                .thenReturn(List.of(reading));

        List<AgpHourlyBucket> result = useCase.execute(patientId, from, to);

        assertThat(result).hasSize(24);
        assertThat(result.stream().filter(b -> b.getHour() == 8).findFirst().orElseThrow().getReadingCount())
                .isEqualTo(1);
    }
}
