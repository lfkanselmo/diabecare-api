package com.diabecare.presentation.mapper;

import com.diabecare.application.dto.GlucoseStatsRecord;
import com.diabecare.presentation.dto.response.GlucoseStatsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GlucoseStatsPresentationMapper")
class GlucoseStatsPresentationMapperTest {

    private final GlucoseStatsPresentationMapper mapper = new GlucoseStatsPresentationMapperImpl();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("mapea todos los campos correctamente")
        void mapsAllFieldsCorrectly() {
            GlucoseStatsRecord record = new GlucoseStatsRecord(
                    BigDecimal.valueOf(120), BigDecimal.valueOf(15), BigDecimal.valueOf(12.5),
                    BigDecimal.valueOf(6.5), BigDecimal.valueOf(75), BigDecimal.valueOf(10),
                    BigDecimal.valueOf(15), 30);

            GlucoseStatsResponse response = mapper.toResponse(record);

            assertThat(response.average()).isEqualByComparingTo(BigDecimal.valueOf(120));
            assertThat(response.standardDeviation()).isEqualByComparingTo(BigDecimal.valueOf(15));
            assertThat(response.timeInRangePercent()).isEqualByComparingTo(BigDecimal.valueOf(75));
            assertThat(response.totalReadings()).isEqualTo(30);
        }
    }
}