package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.infrastructure.persistence.entity.MenstrualCycleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MenstrualCyclePersistenceMapper")
class MenstrualCyclePersistenceMapperTest {

    private final MenstrualCyclePersistenceMapper mapper = new MenstrualCyclePersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando cycleId a id")
        void mapsAllFieldsRenamingCycleIdToId() {
            UUID patientId = UUID.randomUUID();
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.of(2026, 6, 1), "ciclo regular");

            MenstrualCycleEntity entity = mapper.toEntity(cycle);

            assertThat(entity.getId()).isEqualTo(cycle.getCycleId());
            assertThat(entity.getPatientId()).isEqualTo(patientId);
            assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(entity.getEndDate()).isNull();
            assertThat(entity.getNotes()).isEqualTo("ciclo regular");
        }

        @Test
        @DisplayName("mapea correctamente un ciclo ya finalizado, con su fecha de fin")
        void mapsFinishedCycleWithEndDate() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    UUID.randomUUID(), LocalDate.of(2026, 6, 1), null);
            cycle.finish(LocalDate.of(2026, 6, 6));

            MenstrualCycleEntity entity = mapper.toEntity(cycle);

            assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 6));
        }

        @Test
        @DisplayName("ignora createdAt, dejando que la auditoría de JPA lo gestione")
        void ignoresCreatedAt() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    UUID.randomUUID(), LocalDate.of(2026, 6, 1), null);

            MenstrualCycleEntity entity = mapper.toEntity(cycle);

            assertThat(entity.getCreatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando id a cycleId")
        void mapsAllFieldsRenamingIdToCycleId() {
            UUID id = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();

            MenstrualCycleEntity entity = MenstrualCycleEntity.builder()
                    .id(id)
                    .patientId(patientId)
                    .startDate(LocalDate.of(2026, 6, 1))
                    .endDate(LocalDate.of(2026, 6, 6))
                    .notes("ciclo registrado")
                    .build();

            MenstrualCycle cycle = mapper.toDomain(entity);

            assertThat(cycle.getCycleId()).isEqualTo(id);
            assertThat(cycle.getPatientId()).isEqualTo(patientId);
            assertThat(cycle.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(cycle.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 6));
            assertThat(cycle.isOngoing()).isFalse();
        }

        @Test
        @DisplayName("mapea correctamente un ciclo en curso, sin fecha de fin")
        void mapsOngoingCycleWithoutEndDate() {
            MenstrualCycleEntity entity = MenstrualCycleEntity.builder()
                    .id(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .startDate(LocalDate.of(2026, 6, 1))
                    .endDate(null)
                    .build();

            MenstrualCycle cycle = mapper.toDomain(entity);

            assertThat(cycle.isOngoing()).isTrue();
        }
    }
}