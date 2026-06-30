package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.VitalSign;
import com.diabecare.infrastructure.persistence.entity.VitalSignEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("VitalSignPersistenceMapper")
class VitalSignPersistenceMapperTest {

    private final VitalSignPersistenceMapper mapper = new VitalSignPersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando vitalId a id")
        void mapsAllFieldsRenamingVitalIdToId() {
            UUID vitalId = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();
            LocalDateTime measuredAt = LocalDateTime.now();

            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(vitalId)
                    .patientId(patientId)
                    .weightKg(BigDecimal.valueOf(70))
                    .heightCm(BigDecimal.valueOf(170))
                    .systolicBp(120)
                    .diastolicBp(80)
                    .heartRate(70)
                    .hba1c(BigDecimal.valueOf(6.5))
                    .measuredAt(measuredAt)
                    .notes("control rutinario")
                    .build();

            VitalSignEntity entity = mapper.toEntity(vitalSign);

            assertThat(entity.getId()).isEqualTo(vitalId);
            assertThat(entity.getPatientId()).isEqualTo(patientId);
            assertThat(entity.getWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(70));
            assertThat(entity.getSystolicBp()).isEqualTo(120);
            assertThat(entity.getMeasuredAt()).isEqualTo(measuredAt);
            assertThat(entity.getNotes()).isEqualTo("control rutinario");
        }

        @Test
        @DisplayName("ignora createdAt y updatedAt, dejando que la auditoría de JPA los gestione")
        void ignoresCreatedAtAndUpdatedAt() {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .weightKg(BigDecimal.valueOf(70))
                    .measuredAt(LocalDateTime.now())
                    .build();

            VitalSignEntity entity = mapper.toEntity(vitalSign);

            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("mapea correctamente cuando los campos opcionales son nulos")
        void mapsCorrectlyWhenOptionalFieldsAreNull() {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .measuredAt(LocalDateTime.now())
                    .build();

            VitalSignEntity entity = mapper.toEntity(vitalSign);

            assertThat(entity.getWeightKg()).isNull();
            assertThat(entity.getSystolicBp()).isNull();
            assertThat(entity.getHba1c()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando id a vitalId")
        void mapsAllFieldsRenamingIdToVitalId() {
            UUID id = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();

            VitalSignEntity entity = VitalSignEntity.builder()
                    .id(id)
                    .patientId(patientId)
                    .weightKg(BigDecimal.valueOf(65))
                    .heightCm(BigDecimal.valueOf(160))
                    .measuredAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            VitalSign vitalSign = mapper.toDomain(entity);

            assertThat(vitalSign.getVitalId()).isEqualTo(id);
            assertThat(vitalSign.getPatientId()).isEqualTo(patientId);
            assertThat(vitalSign.getWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(65));
            assertThat(vitalSign.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(160));
        }
    }
}