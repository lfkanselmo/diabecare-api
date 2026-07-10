package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidPatientDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Patient")
class PatientTest {

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("crea un paciente válido con valores por defecto correctos")
        void createsValidPatientWithDefaults() {
            Patient patient = Patient.create(
                    userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));

            assertThat(patient.getPatientId()).isNotNull();
            assertThat(patient.getFullName()).isEqualTo("Ana García");
            assertThat(patient.getTargetGlucoseMin()).isEqualByComparingTo(BigDecimal.valueOf(70));
            assertThat(patient.getTargetGlucoseMax()).isEqualByComparingTo(BigDecimal.valueOf(180));
            assertThat(patient.getActivityLevel()).isEqualTo(ActivityLevel.SEDENTARY);
            assertThat(patient.getPreferredGlucoseUnit()).isEqualTo(GlucoseUnit.MG_DL);
            assertThat(patient.getBiologicalSex()).isEqualTo(BiologicalSex.NOT_SPECIFIED);
        }

        @Test
        @DisplayName("rechaza nombre nulo")
        void rejectsNullName() {
            assertThatThrownBy(() -> Patient.create(
                    userId, null, LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(170)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("nombre completo");
        }

        @Test
        @DisplayName("rechaza nombre en blanco")
        void rejectsBlankName() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "   ", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(170)))
                    .isInstanceOf(InvalidPatientDataException.class);
        }

        @Test
        @DisplayName("rechaza nombre de más de 150 caracteres")
        void rejectsNameLongerThan150Chars() {
            String longName = "A".repeat(151);
            assertThatThrownBy(() -> Patient.create(
                    userId, longName, LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(170)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("150 caracteres");
        }

        @Test
        @DisplayName("acepta nombre de exactamente 150 caracteres")
        void acceptsNameOfExactly150Chars() {
            String name = "A".repeat(150);
            assertThatCode(() -> Patient.create(
                    userId, name, LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(170)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechaza fecha de nacimiento nula")
        void rejectsNullDateOfBirth() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "Ana", null,
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(170)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("nacimiento");
        }

        @Test
        @DisplayName("rechaza fecha de nacimiento futura")
        void rejectsFutureDateOfBirth() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "Ana", LocalDate.now().plusDays(1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(170)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("futura");
        }

        @Test
        @DisplayName("rechaza fecha de diagnóstico nula")
        void rejectsNullDiagnosisDate() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, null, BigDecimal.valueOf(170)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("diagnóstico");
        }

        @Test
        @DisplayName("rechaza fecha de diagnóstico anterior al nacimiento")
        void rejectsDiagnosisBeforeBirth() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(1989, 12, 31), BigDecimal.valueOf(170)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("anterior al nacimiento");
        }

        @Test
        @DisplayName("rechaza fecha de diagnóstico futura")
        void rejectsFutureDiagnosisDate() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.now().plusDays(1), BigDecimal.valueOf(170)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("futura");
        }

        @Test
        @DisplayName("acepta fecha de diagnóstico igual a la de nacimiento")
        void acceptsDiagnosisSameDayAsBirth() {
            assertThatCode(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_1, LocalDate.of(1990, 1, 1), BigDecimal.valueOf(170)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechaza talla nula")
        void rejectsNullHeight() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), null))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("talla");
        }

        @Test
        @DisplayName("rechaza talla por debajo de 50 cm")
        void rejectsHeightBelow50() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(49)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("50 y 250");
        }

        @Test
        @DisplayName("rechaza talla por encima de 250 cm")
        void rejectsHeightAbove250() {
            assertThatThrownBy(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(251)))
                    .isInstanceOf(InvalidPatientDataException.class);
        }

        @Test
        @DisplayName("acepta los límites exactos de talla (50 y 250 cm)")
        void acceptsExactHeightBoundaries() {
            assertThatCode(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(50)))
                    .doesNotThrowAnyException();
            assertThatCode(() -> Patient.create(
                    userId, "Ana", LocalDate.of(1990, 1, 1),
                    DiabetesType.TYPE_2, LocalDate.of(2015, 1, 1), BigDecimal.valueOf(250)))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getAge")
    class GetAge {

        @Test
        @DisplayName("calcula la edad correctamente en base a la fecha de nacimiento")
        void calculatesAgeCorrectly() {
            LocalDate birthDate = LocalDate.now().minusYears(30).minusDays(1);
            Patient patient = Patient.create(
                    userId, "Ana", birthDate, DiabetesType.TYPE_1,
                    birthDate.plusYears(5), BigDecimal.valueOf(165));

            assertThat(patient.getAge()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("isGlucoseInRange")
    class IsGlucoseInRange {

        @Test
        @DisplayName("retorna true para un valor dentro del rango objetivo")
        void returnsTrueWhenInRange() {
            Patient patient = validPatient();
            assertThat(patient.isGlucoseInRange(BigDecimal.valueOf(100))).isTrue();
        }

        @Test
        @DisplayName("retorna true en los límites exactos del rango")
        void returnsTrueAtExactBoundaries() {
            Patient patient = validPatient();
            assertThat(patient.isGlucoseInRange(BigDecimal.valueOf(70))).isTrue();
            assertThat(patient.isGlucoseInRange(BigDecimal.valueOf(180))).isTrue();
        }

        @Test
        @DisplayName("retorna false para un valor fuera del rango")
        void returnsFalseWhenOutOfRange() {
            Patient patient = validPatient();
            assertThat(patient.isGlucoseInRange(BigDecimal.valueOf(69))).isFalse();
            assertThat(patient.isGlucoseInRange(BigDecimal.valueOf(181))).isFalse();
        }
    }

    @Nested
    @DisplayName("updateHeight")
    class UpdateHeight {

        @Test
        @DisplayName("actualiza la talla correctamente")
        void updatesHeightCorrectly() {
            Patient patient = validPatient();
            patient.updateHeight(BigDecimal.valueOf(170));

            assertThat(patient.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(170));
        }

        @Test
        @DisplayName("rechaza talla por debajo de 50 cm")
        void rejectsHeightBelow50() {
            Patient patient = validPatient();
            assertThatThrownBy(() -> patient.updateHeight(BigDecimal.valueOf(49)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("50 y 250");
        }

        @Test
        @DisplayName("rechaza talla por encima de 250 cm")
        void rejectsHeightAbove250() {
            Patient patient = validPatient();
            assertThatThrownBy(() -> patient.updateHeight(BigDecimal.valueOf(251)))
                    .isInstanceOf(InvalidPatientDataException.class);
        }

        @Test
        @DisplayName("acepta los límites exactos (50 y 250 cm)")
        void acceptsExactBoundaries() {
            Patient patient = validPatient();
            assertThatCode(() -> patient.updateHeight(BigDecimal.valueOf(50))).doesNotThrowAnyException();
            assertThatCode(() -> patient.updateHeight(BigDecimal.valueOf(250))).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("updateGlucoseTarget")
    class UpdateGlucoseTarget {

        @Test
        @DisplayName("actualiza el rango correctamente")
        void updatesRangeCorrectly() {
            Patient patient = validPatient();
            patient.updateGlucoseTarget(BigDecimal.valueOf(80), BigDecimal.valueOf(160));

            assertThat(patient.getTargetGlucoseMin()).isEqualByComparingTo(BigDecimal.valueOf(80));
            assertThat(patient.getTargetGlucoseMax()).isEqualByComparingTo(BigDecimal.valueOf(160));
        }

        @Test
        @DisplayName("rechaza cuando el mínimo es mayor o igual al máximo")
        void rejectsMinGreaterOrEqualThanMax() {
            Patient patient = validPatient();
            assertThatThrownBy(() -> patient.updateGlucoseTarget(
                    BigDecimal.valueOf(180), BigDecimal.valueOf(180)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("menor que el máximo");
        }

        @Test
        @DisplayName("rechaza mínimo por debajo de 50 mg/dL")
        void rejectsMinBelow50() {
            Patient patient = validPatient();
            assertThatThrownBy(() -> patient.updateGlucoseTarget(
                    BigDecimal.valueOf(49), BigDecimal.valueOf(180)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("50 mg/dL");
        }

        @Test
        @DisplayName("rechaza máximo por encima de 400 mg/dL")
        void rejectsMaxAbove400() {
            Patient patient = validPatient();
            assertThatThrownBy(() -> patient.updateGlucoseTarget(
                    BigDecimal.valueOf(100), BigDecimal.valueOf(401)))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("400 mg/dL");
        }
    }

    @Nested
    @DisplayName("updateDailyCalorieGoal")
    class UpdateDailyCalorieGoal {

        @Test
        @DisplayName("actualiza la meta calórica correctamente")
        void updatesGoalCorrectly() {
            Patient patient = validPatient();
            patient.updateDailyCalorieGoal(2000);
            assertThat(patient.getDailyCalorieGoal()).isEqualTo(2000);
        }

        @Test
        @DisplayName("permite establecer la meta en null")
        void allowsNullGoal() {
            Patient patient = validPatient();
            assertThatCode(() -> patient.updateDailyCalorieGoal(null)).doesNotThrowAnyException();
            assertThat(patient.getDailyCalorieGoal()).isNull();
        }

        @Test
        @DisplayName("rechaza meta por debajo de 500 kcal")
        void rejectsBelow500() {
            Patient patient = validPatient();
            assertThatThrownBy(() -> patient.updateDailyCalorieGoal(499))
                    .isInstanceOf(InvalidPatientDataException.class)
                    .hasMessageContaining("500 y 5000");
        }

        @Test
        @DisplayName("rechaza meta por encima de 5000 kcal")
        void rejectsAbove5000() {
            Patient patient = validPatient();
            assertThatThrownBy(() -> patient.updateDailyCalorieGoal(5001))
                    .isInstanceOf(InvalidPatientDataException.class);
        }

        @Test
        @DisplayName("acepta los límites exactos (500 y 5000 kcal)")
        void acceptsExactBoundaries() {
            Patient patient = validPatient();
            assertThatCode(() -> patient.updateDailyCalorieGoal(500)).doesNotThrowAnyException();
            assertThatCode(() -> patient.updateDailyCalorieGoal(5000)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("otros updaters simples")
    class OtherUpdaters {

        @Test
        @DisplayName("updateActivityLevel cambia el nivel de actividad")
        void updateActivityLevelWorks() {
            Patient patient = validPatient();
            patient.updateActivityLevel(ActivityLevel.VERY_ACTIVE);
            assertThat(patient.getActivityLevel()).isEqualTo(ActivityLevel.VERY_ACTIVE);
        }

        @Test
        @DisplayName("updatePreferredGlucoseUnit cambia la unidad preferida")
        void updatePreferredGlucoseUnitWorks() {
            Patient patient = validPatient();
            patient.updatePreferredGlucoseUnit(GlucoseUnit.MMOL_L);
            assertThat(patient.getPreferredGlucoseUnit()).isEqualTo(GlucoseUnit.MMOL_L);
        }

        @Test
        @DisplayName("updateInsulinProfile actualiza los 3 campos relacionados")
        void updateInsulinProfileWorks() {
            Patient patient = validPatient();
            patient.updateInsulinProfile(BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(120));

            assertThat(patient.getInsulinSensitivityFactor()).isEqualByComparingTo(BigDecimal.valueOf(50));
            assertThat(patient.getInsulinToCarbRatio()).isEqualByComparingTo(BigDecimal.valueOf(10));
            assertThat(patient.getTargetGlucoseForCorrection()).isEqualByComparingTo(BigDecimal.valueOf(120));
        }

        @Test
        @DisplayName("updateBiologicalSex cambia el sexo biológico")
        void updateBiologicalSexWorks() {
            Patient patient = validPatient();
            patient.updateBiologicalSex(BiologicalSex.FEMALE);
            assertThat(patient.getBiologicalSex()).isEqualTo(BiologicalSex.FEMALE);
        }
    }

    @Nested
    @DisplayName("isFemale")
    class IsFemale {

        @Test
        @DisplayName("retorna true cuando el sexo biológico es FEMALE")
        void returnsTrueWhenFemale() {
            Patient patient = validPatient();
            patient.updateBiologicalSex(BiologicalSex.FEMALE);
            assertThat(patient.isFemale()).isTrue();
        }

        @Test
        @DisplayName("retorna false cuando el sexo biológico es MALE o NOT_SPECIFIED")
        void returnsFalseWhenNotFemale() {
            Patient patient = validPatient();
            assertThat(patient.isFemale()).isFalse();

            patient.updateBiologicalSex(BiologicalSex.MALE);
            assertThat(patient.isFemale()).isFalse();
        }
    }


    private Patient validPatient() {
        return Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}
