package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.Patient;
import com.diabecare.infrastructure.persistence.entity.PatientEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PatientPersistenceMapper")
class PatientPersistenceMapperTest {

    private final PatientPersistenceMapper mapper = new PatientPersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo los enums a String")
        void mapsAllFieldsConvertingEnumsToString() {
            UUID userId = UUID.randomUUID();
            Patient patient = Patient.create(
                    userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
            patient.updateActivityLevel(ActivityLevel.VERY_ACTIVE);
            patient.updatePreferredGlucoseUnit(GlucoseUnit.MMOL_L);
            patient.updateBiologicalSex(BiologicalSex.FEMALE);

            PatientEntity entity = mapper.toEntity(patient);

            assertThat(entity.getId()).isEqualTo(patient.getPatientId());
            assertThat(entity.getUserId()).isEqualTo(userId);
            assertThat(entity.getFullName()).isEqualTo("Ana García");
            assertThat(entity.getDiabetesType()).isEqualTo("TYPE_1");
            assertThat(entity.getActivityLevel()).isEqualTo("VERY_ACTIVE");
            assertThat(entity.getPreferredGlucoseUnit()).isEqualTo("MMOL_L");
            assertThat(entity.getBiologicalSex()).isEqualTo("FEMALE");
        }

        @Test
        @DisplayName("mapea targetGlucoseForCorrection a targetGlucoseCorrection correctamente")
        void mapsTargetGlucoseForCorrectionToTargetGlucoseCorrection() {
            Patient patient = Patient.create(
                    UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
            patient.updateInsulinProfile(BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(120));

            PatientEntity entity = mapper.toEntity(patient);

            assertThat(entity.getTargetGlucoseCorrection()).isEqualByComparingTo(BigDecimal.valueOf(120));
        }

        @Test
        @DisplayName("mapea NOT_SPECIFIED como String cuando el sexo biológico no fue establecido explícitamente")
        void mapsNotSpecifiedWhenBiologicalSexDefaultsToNotSpecified() {
            Patient patient = Patient.create(
                    UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));

            PatientEntity entity = mapper.toEntity(patient);

            assertThat(entity.getBiologicalSex()).isEqualTo("NOT_SPECIFIED");
        }

        @Test
        @DisplayName("ignora createdAt y updatedAt, dejando que la auditoría de JPA los gestione")
        void ignoresCreatedAtAndUpdatedAt() {
            Patient patient = Patient.create(
                    UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));

            PatientEntity entity = mapper.toEntity(patient);

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
            UUID userId = UUID.randomUUID();

            PatientEntity entity = PatientEntity.builder()
                    .id(id)
                    .userId(userId)
                    .fullName("Ana García")
                    .dateOfBirth(LocalDate.of(1990, 5, 10))
                    .diabetesType("TYPE_2")
                    .diagnosisDate(LocalDate.of(2015, 1, 1))
                    .heightCm(BigDecimal.valueOf(165))
                    .targetGlucoseMin(BigDecimal.valueOf(70))
                    .targetGlucoseMax(BigDecimal.valueOf(180))
                    .activityLevel("MODERATELY_ACTIVE")
                    .preferredGlucoseUnit("MG_DL")
                    .biologicalSex("MALE")
                    .build();

            Patient patient = mapper.toDomain(entity);

            assertThat(patient.getPatientId()).isEqualTo(id);
            assertThat(patient.getUserId()).isEqualTo(userId);
            assertThat(patient.getDiabetesType()).isEqualTo(DiabetesType.TYPE_2);
            assertThat(patient.getActivityLevel()).isEqualTo(ActivityLevel.MODERATELY_ACTIVE);
            assertThat(patient.getPreferredGlucoseUnit()).isEqualTo(GlucoseUnit.MG_DL);
            assertThat(patient.getBiologicalSex()).isEqualTo(BiologicalSex.MALE);
        }

        @Test
        @DisplayName("mapea NOT_SPECIFIED cuando biologicalSex en la BD es nulo (pacientes antiguos)")
        void mapsNotSpecifiedWhenBiologicalSexIsNullInDatabase() {
            PatientEntity entity = PatientEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .fullName("Paciente Antiguo")
                    .dateOfBirth(LocalDate.of(1980, 1, 1))
                    .diabetesType("TYPE_2")
                    .diagnosisDate(LocalDate.of(2010, 1, 1))
                    .heightCm(BigDecimal.valueOf(170))
                    .targetGlucoseMin(BigDecimal.valueOf(70))
                    .targetGlucoseMax(BigDecimal.valueOf(180))
                    .activityLevel("SEDENTARY")
                    .preferredGlucoseUnit("MG_DL")
                    .biologicalSex(null)
                    .build();

            Patient patient = mapper.toDomain(entity);

            assertThat(patient.getBiologicalSex()).isEqualTo(BiologicalSex.NOT_SPECIFIED);
        }

        @Test
        @DisplayName("mapea targetGlucoseCorrection a targetGlucoseForCorrection correctamente")
        void mapsTargetGlucoseCorrectionToTargetGlucoseForCorrection() {
            PatientEntity entity = PatientEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .fullName("Ana García")
                    .dateOfBirth(LocalDate.of(1990, 5, 10))
                    .diabetesType("TYPE_1")
                    .diagnosisDate(LocalDate.of(2010, 1, 1))
                    .heightCm(BigDecimal.valueOf(165))
                    .targetGlucoseMin(BigDecimal.valueOf(70))
                    .targetGlucoseMax(BigDecimal.valueOf(180))
                    .activityLevel("SEDENTARY")
                    .preferredGlucoseUnit("MG_DL")
                    .targetGlucoseCorrection(BigDecimal.valueOf(120))
                    .build();

            Patient patient = mapper.toDomain(entity);

            assertThat(patient.getTargetGlucoseForCorrection()).isEqualByComparingTo(BigDecimal.valueOf(120));
        }
    }
}