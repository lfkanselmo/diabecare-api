package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.infrastructure.persistence.entity.MedicationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MedicationPersistenceMapper")
class MedicationPersistenceMapperTest {

    private final MedicationPersistenceMapper mapper = new MedicationPersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo los 3 enums a String")
        void mapsAllFieldsConvertingThreeEnumsToString() {
            UUID patientId = UUID.randomUUID();
            Medication medication = Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                    DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), "con comidas");

            MedicationEntity entity = mapper.toEntity(medication);

            assertThat(entity.getId()).isEqualTo(medication.getMedicationId());
            assertThat(entity.getPatientId()).isEqualTo(patientId);
            assertThat(entity.getName()).isEqualTo("Metformina");
            assertThat(entity.getType()).isEqualTo("ORAL");
            assertThat(entity.getDose()).isEqualByComparingTo(BigDecimal.valueOf(500));
            assertThat(entity.getDoseUnit()).isEqualTo("MG");
            assertThat(entity.getFrequency()).isEqualTo("TWICE_DAILY");
            assertThat(entity.isActive()).isTrue();
            assertThat(entity.getNotes()).isEqualTo("con comidas");
        }

        @Test
        @DisplayName("mapea correctamente un medicamento ya desactivado")
        void mapsDeactivatedMedicationCorrectly() {
            Medication medication = Medication.create(
                    UUID.randomUUID(), "Insulina", MedicationType.INSULIN_BASAL, BigDecimal.valueOf(10),
                    DoseUnit.UNITS, MedicationFrequency.ONCE_DAILY, LocalDate.of(2026, 1, 1), null);
            medication.deactivate();

            MedicationEntity entity = mapper.toEntity(medication);

            assertThat(entity.isActive()).isFalse();
            assertThat(entity.getEndDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("ignora createdAt y updatedAt, dejando que la auditoría de JPA los gestione")
        void ignoresCreatedAtAndUpdatedAt() {
            Medication medication = Medication.create(
                    UUID.randomUUID(), "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                    DoseUnit.MG, MedicationFrequency.ONCE_DAILY, LocalDate.of(2026, 1, 1), null);

            MedicationEntity entity = mapper.toEntity(medication);

            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo los 3 String a enums")
        void mapsAllFieldsConvertingThreeStringsToEnums() {
            UUID id = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();

            MedicationEntity entity = MedicationEntity.builder()
                    .id(id)
                    .patientId(patientId)
                    .name("Metformina")
                    .type("ORAL")
                    .dose(BigDecimal.valueOf(500))
                    .doseUnit("MG")
                    .frequency("TWICE_DAILY")
                    .startDate(LocalDate.of(2026, 1, 1))
                    .active(true)
                    .build();

            Medication medication = mapper.toDomain(entity);

            assertThat(medication.getMedicationId()).isEqualTo(id);
            assertThat(medication.getType()).isEqualTo(MedicationType.ORAL);
            assertThat(medication.getDoseUnit()).isEqualTo(DoseUnit.MG);
            assertThat(medication.getFrequency()).isEqualTo(MedicationFrequency.TWICE_DAILY);
            assertThat(medication.isActive()).isTrue();
        }
    }
}