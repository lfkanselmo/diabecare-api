package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.VitalSign;
import com.diabecare.presentation.dto.response.VitalSignResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("VitalSignPresentationMapper")
class VitalSignPresentationMapperTest {

    private final VitalSignPresentationMapper mapper = new VitalSignPresentationMapperImpl();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("calcula bmi y bmiCategory correctamente cuando hay peso y talla")
        void calculatesBmiAndCategoryCorrectlyWhenWeightAndHeightPresent() {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .weightKg(BigDecimal.valueOf(70))
                    .heightCm(BigDecimal.valueOf(170))
                    .measuredAt(LocalDateTime.now())
                    .build();

            VitalSignResponse response = mapper.toResponse(vitalSign);

            assertThat(response.bmi()).isNotNull();
            assertThat(response.bmiCategory()).isEqualTo("NORMAL");
        }

        @Test
        @DisplayName("mapea bmi y bmiCategory como null cuando falta la talla")
        void mapsBmiAndCategoryAsNullWhenHeightMissing() {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .weightKg(BigDecimal.valueOf(70))
                    .heightCm(null)
                    .measuredAt(LocalDateTime.now())
                    .build();

            VitalSignResponse response = mapper.toResponse(vitalSign);

            assertThat(response.bmi()).isNull();
            assertThat(response.bmiCategory()).isNull();
        }

        @Test
        @DisplayName("mapea bmiCategory como OBESE para un BMI alto")
        void mapsBmiCategoryAsObeseForHighBmi() {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .weightKg(BigDecimal.valueOf(110))
                    .heightCm(BigDecimal.valueOf(170))
                    .measuredAt(LocalDateTime.now())
                    .build();

            VitalSignResponse response = mapper.toResponse(vitalSign);

            assertThat(response.bmiCategory()).isEqualTo("OBESE");
        }
    }
}