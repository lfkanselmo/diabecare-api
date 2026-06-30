package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CycleSymptom;
import com.diabecare.domain.model.CycleSymptomEntry;
import com.diabecare.domain.model.FlowIntensity;
import com.diabecare.domain.model.SymptomSeverity;
import com.diabecare.infrastructure.persistence.entity.CycleDayEntryEntity;
import com.diabecare.infrastructure.persistence.entity.CycleDaySymptomEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CycleDayEntryPersistenceMapper")
class CycleDayEntryPersistenceMapperTest {

    private final CycleDayEntryPersistenceMapper mapper = new CycleDayEntryPersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando dayEntryId a id")
        void mapsAllFieldsRenamingDayEntryIdToId() {
            UUID dayEntryId = UUID.randomUUID();
            UUID cycleId = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();

            CycleDayEntry entry = CycleDayEntry.builder()
                    .dayEntryId(dayEntryId)
                    .cycleId(cycleId)
                    .patientId(patientId)
                    .entryDate(LocalDate.of(2026, 6, 1))
                    .flowIntensity(FlowIntensity.MODERATE)
                    .notes("nota")
                    .symptoms(List.of())
                    .build();

            CycleDayEntryEntity entity = mapper.toEntity(entry);

            assertThat(entity.getId()).isEqualTo(dayEntryId);
            assertThat(entity.getCycleId()).isEqualTo(cycleId);
            assertThat(entity.getPatientId()).isEqualTo(patientId);
            assertThat(entity.getEntryDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(entity.getFlowIntensity()).isEqualTo("MODERATE");
            assertThat(entity.getNotes()).isEqualTo("nota");
        }

        @Test
        @DisplayName("ignora createdAt y symptoms, ya que se gestionan por separado")
        void ignoresCreatedAtAndSymptoms() {
            CycleDayEntry entry = CycleDayEntry.create(
                    UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2026, 6, 1),
                    FlowIntensity.LIGHT, null,
                    List.of(CycleSymptomEntry.builder()
                            .symptom(CycleSymptom.CRAMPS)
                            .severity(SymptomSeverity.MILD)
                            .build()));

            CycleDayEntryEntity entity = mapper.toEntity(entry);

            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getSymptoms()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, renombrando id a dayEntryId")
        void mapsAllFieldsRenamingIdToDayEntryId() {
            UUID id = UUID.randomUUID();
            UUID cycleId = UUID.randomUUID();

            CycleDayEntryEntity entity = CycleDayEntryEntity.builder()
                    .id(id)
                    .cycleId(cycleId)
                    .patientId(UUID.randomUUID())
                    .entryDate(LocalDate.of(2026, 6, 1))
                    .flowIntensity("HEAVY")
                    .notes("día pesado")
                    .symptoms(List.of())
                    .build();

            CycleDayEntry domain = mapper.toDomain(entity);

            assertThat(domain.getDayEntryId()).isEqualTo(id);
            assertThat(domain.getCycleId()).isEqualTo(cycleId);
            assertThat(domain.getFlowIntensity()).isEqualTo(FlowIntensity.HEAVY);
            assertThat(domain.getNotes()).isEqualTo("día pesado");
        }

        @Test
        @DisplayName("mapea automáticamente la lista de síntomas, convirtiendo los códigos a enums")
        void mapsSymptomsListConvertingCodesToEnums() {
            CycleDaySymptomEntity symptomEntity = CycleDaySymptomEntity.builder()
                    .id(UUID.randomUUID())
                    .symptomCode("HEADACHE")
                    .severity("SEVERE")
                    .build();

            CycleDayEntryEntity entity = CycleDayEntryEntity.builder()
                    .id(UUID.randomUUID())
                    .cycleId(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .entryDate(LocalDate.of(2026, 6, 1))
                    .flowIntensity("MODERATE")
                    .symptoms(List.of(symptomEntity))
                    .build();

            CycleDayEntry domain = mapper.toDomain(entity);

            assertThat(domain.getSymptoms()).hasSize(1);
            assertThat(domain.getSymptoms().get(0).getSymptom()).isEqualTo(CycleSymptom.HEADACHE);
            assertThat(domain.getSymptoms().get(0).getSeverity()).isEqualTo(SymptomSeverity.SEVERE);
        }
    }
}