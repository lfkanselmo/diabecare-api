package com.diabecare.presentation.mapper;

import com.diabecare.application.dto.DailySummaryRecord;
import com.diabecare.presentation.dto.response.DailySummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DailySummaryPresentationMapper")
class DailySummaryPresentationMapperTest {

    private final DailySummaryPresentationMapper mapper = new DailySummaryPresentationMapperImpl();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("mapea todos los campos correctamente")
        void mapsAllFieldsCorrectly() {
            DailySummaryRecord record = new DailySummaryRecord(
                    LocalDate.of(2026, 6, 15),
                    BigDecimal.valueOf(1800), BigDecimal.valueOf(200),
                    BigDecimal.valueOf(80), BigDecimal.valueOf(60),
                    2000, false);

            DailySummaryResponse response = mapper.toResponse(record);

            assertThat(response.date()).isEqualTo(LocalDate.of(2026, 6, 15));
            assertThat(response.totalCalories()).isEqualByComparingTo(BigDecimal.valueOf(1800));
            assertThat(response.calorieGoal()).isEqualTo(2000);
            assertThat(response.goalReached()).isFalse();
        }
    }
}