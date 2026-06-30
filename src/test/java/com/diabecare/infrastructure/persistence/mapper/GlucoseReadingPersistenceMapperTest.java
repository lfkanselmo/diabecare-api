package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.infrastructure.persistence.entity.GlucoseReadingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GlucoseReadingPersistenceMapper")
class GlucoseReadingPersistenceMapperTest {

    private final GlucoseReadingPersistenceMapper mapper = new GlucoseReadingPersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo los enums a String")
        void mapsAllFieldsConvertingEnumsToString() {
            UUID patientId = UUID.randomUUID();
            GlucoseReading reading = GlucoseReading.create(
                    patientId, BigDecimal.valueOf(120), GlucoseUnit.MG_DL,
                    ReadingType.FASTING, LocalDateTime.now().minusMinutes(5), "nota", "glucómetro X");

            GlucoseReadingEntity entity = mapper.toEntity(reading);

            assertThat(entity.getId()).isEqualTo(reading.getReadingId());
            assertThat(entity.getPatientId()).isEqualTo(patientId);
            assertThat(entity.getValue()).isEqualByComparingTo(BigDecimal.valueOf(120));
            assertThat(entity.getUnit()).isEqualTo("MG_DL");
            assertThat(entity.getReadingType()).isEqualTo("FASTING");
            assertThat(entity.getNotes()).isEqualTo("nota");
            assertThat(entity.getDeviceSource()).isEqualTo("glucómetro X");
        }

        @Test
        @DisplayName("ignora createdAt y updatedAt, dejando que la auditoría de JPA los gestione")
        void ignoresCreatedAtAndUpdatedAt() {
            GlucoseReading reading = GlucoseReading.create(
                    UUID.randomUUID(), BigDecimal.valueOf(100), GlucoseUnit.MG_DL,
                    ReadingType.RANDOM, LocalDateTime.now().minusMinutes(5), null, null);

            GlucoseReadingEntity entity = mapper.toEntity(reading);

            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo los String a enums")
        void mapsAllFieldsConvertingStringsToEnums() {
            UUID id = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();

            GlucoseReadingEntity entity = GlucoseReadingEntity.builder()
                    .id(id)
                    .patientId(patientId)
                    .value(BigDecimal.valueOf(95))
                    .unit("MMOL_L")
                    .readingType("POST_MEAL")
                    .measuredAt(LocalDateTime.now())
                    .build();

            GlucoseReading reading = mapper.toDomain(entity);

            assertThat(reading.getReadingId()).isEqualTo(id);
            assertThat(reading.getPatientId()).isEqualTo(patientId);
            assertThat(reading.getUnit()).isEqualTo(GlucoseUnit.MMOL_L);
            assertThat(reading.getReadingType()).isEqualTo(ReadingType.POST_MEAL);
        }
    }
}