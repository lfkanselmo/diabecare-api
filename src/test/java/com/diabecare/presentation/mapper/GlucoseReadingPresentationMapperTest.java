package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.presentation.dto.response.GlucoseReadingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GlucoseReadingPresentationMapper")
class GlucoseReadingPresentationMapperTest {

    private final GlucoseReadingPresentationMapper mapper = new GlucoseReadingPresentationMapperImpl();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("convierte unit, readingType y status a String correctamente")
        void convertsUnitReadingTypeAndStatusToString() {
            GlucoseReading reading = GlucoseReading.create(
                    UUID.randomUUID(), BigDecimal.valueOf(120), GlucoseUnit.MG_DL,
                    ReadingType.FASTING, LocalDateTime.now().minusMinutes(5), "nota", "glucómetro");

            GlucoseReadingResponse response = mapper.toResponse(reading);

            assertThat(response.unit()).isEqualTo("MG_DL");
            assertThat(response.readingType()).isEqualTo("FASTING");
            assertThat(response.status()).isEqualTo("NORMAL");
            assertThat(response.notes()).isEqualTo("nota");
            assertThat(response.deviceSource()).isEqualTo("glucómetro");
        }

        @Test
        @DisplayName("calcula el status CRITICALLY_LOW correctamente para un valor muy bajo")
        void calculatesCriticallyLowStatusForVeryLowValue() {
            GlucoseReading reading = GlucoseReading.create(
                    UUID.randomUUID(), BigDecimal.valueOf(40), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now().minusMinutes(5), null, null);

            GlucoseReadingResponse response = mapper.toResponse(reading);

            assertThat(response.status()).isEqualTo("CRITICALLY_LOW");
        }

        @Test
        @DisplayName("calcula el status HIGH correctamente para un valor elevado")
        void calculatesHighStatusForElevatedValue() {
            GlucoseReading reading = GlucoseReading.create(
                    UUID.randomUUID(), BigDecimal.valueOf(220), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now().minusMinutes(5), null, null);

            GlucoseReadingResponse response = mapper.toResponse(reading);

            assertThat(response.status()).isEqualTo("HIGH");
        }
    }
}