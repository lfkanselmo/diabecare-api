package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import com.diabecare.presentation.dto.response.PatientResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PatientPresentationMapper")
class PatientPresentationMapperTest {

    private final PatientPresentationMapper mapper = new PatientPresentationMapperImpl();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("calcula la edad correctamente a partir de la fecha de nacimiento")
        void calculatesAgeCorrectlyFromDateOfBirth() {
            LocalDate birthDate = LocalDate.now().minusYears(30).minusDays(1);
            Patient patient = Patient.create(
                    UUID.randomUUID(), "Ana García", birthDate,
                    DiabetesType.TYPE_1, birthDate.plusYears(5), BigDecimal.valueOf(165));

            PatientResponse response = mapper.toResponse(patient);

            assertThat(response.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("convierte los enums diabetesType, activityLevel y preferredGlucoseUnit a String")
        void convertsEnumsToString() {
            Patient patient = Patient.create(
                    UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(165));

            PatientResponse response = mapper.toResponse(patient);

            assertThat(response.diabetesType()).isEqualTo("TYPE_2");
            assertThat(response.activityLevel()).isEqualTo("SEDENTARY");
            assertThat(response.preferredGlucoseUnit()).isEqualTo("MG_DL");
        }

        @Test
        @DisplayName("mapea biologicalSex a String cuando está establecido")
        void mapsBiologicalSexToStringWhenSet() {
            Patient patient = Patient.create(
                    UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
            patient.updateBiologicalSex(BiologicalSex.FEMALE);

            PatientResponse response = mapper.toResponse(patient);

            assertThat(response.biologicalSex()).isEqualTo("FEMALE");
        }

        @Test
        @DisplayName("mapea biologicalSex a NOT_SPECIFIED cuando es nulo")
        void mapsBiologicalSexToNotSpecifiedWhenNull() {
            Patient patient = Patient.create(
                    UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));

            PatientResponse response = mapper.toResponse(patient);

            assertThat(response.biologicalSex()).isEqualTo("NOT_SPECIFIED");
        }

        @Test
        @DisplayName("mapea todos los campos numéricos y de identificación correctamente")
        void mapsAllNumericAndIdentificationFieldsCorrectly() {
            Patient patient = Patient.create(
                    UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));

            PatientResponse response = mapper.toResponse(patient);

            assertThat(response.patientId()).isEqualTo(patient.getPatientId());
            assertThat(response.fullName()).isEqualTo("Ana García");
            assertThat(response.heightCm()).isEqualByComparingTo(BigDecimal.valueOf(165));
            assertThat(response.targetGlucoseMin()).isEqualByComparingTo(BigDecimal.valueOf(70));
            assertThat(response.targetGlucoseMax()).isEqualByComparingTo(BigDecimal.valueOf(180));
        }
    }
}