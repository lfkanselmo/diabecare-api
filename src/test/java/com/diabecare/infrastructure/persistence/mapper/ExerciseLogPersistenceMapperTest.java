package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import com.diabecare.infrastructure.persistence.entity.ExerciseLogEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExerciseLogPersistenceMapper")
class ExerciseLogPersistenceMapperTest {

    private final ExerciseLogPersistenceMapper mapper = new ExerciseLogPersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo los enums a String")
        void mapsAllFieldsConvertingEnumsToString() {
            UUID patientId = UUID.randomUUID();
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.RUNNING, ExerciseIntensity.HIGH,
                    45, "carrera matutina", LocalDateTime.now().minusMinutes(5), BigDecimal.valueOf(400));

            ExerciseLogEntity entity = mapper.toEntity(log);

            assertThat(entity.getId()).isEqualTo(log.getExerciseId());
            assertThat(entity.getPatientId()).isEqualTo(patientId);
            assertThat(entity.getExerciseType()).isEqualTo("RUNNING");
            assertThat(entity.getIntensity()).isEqualTo("HIGH");
            assertThat(entity.getDurationMinutes()).isEqualTo(45);
            assertThat(entity.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(400));
            assertThat(entity.getNotes()).isEqualTo("carrera matutina");
        }

        @Test
        @DisplayName("ignora createdAt y updatedAt, dejando que la auditoría de JPA los gestione")
        void ignoresCreatedAtAndUpdatedAt() {
            ExerciseLog log = ExerciseLog.create(
                    UUID.randomUUID(), ExerciseType.WALKING, ExerciseIntensity.LOW,
                    30, null, LocalDateTime.now().minusMinutes(5), null);

            ExerciseLogEntity entity = mapper.toEntity(log);

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

            ExerciseLogEntity entity = ExerciseLogEntity.builder()
                    .id(id)
                    .patientId(patientId)
                    .exerciseType("SWIMMING")
                    .intensity("MODERATE")
                    .durationMinutes(60)
                    .caloriesBurned(BigDecimal.valueOf(350))
                    .performedAt(LocalDateTime.now())
                    .build();

            ExerciseLog log = mapper.toDomain(entity);

            assertThat(log.getExerciseId()).isEqualTo(id);
            assertThat(log.getPatientId()).isEqualTo(patientId);
            assertThat(log.getExerciseType()).isEqualTo(ExerciseType.SWIMMING);
            assertThat(log.getIntensity()).isEqualTo(ExerciseIntensity.MODERATE);
            assertThat(log.getDurationMinutes()).isEqualTo(60);
            assertThat(log.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(350));
        }
    }
}