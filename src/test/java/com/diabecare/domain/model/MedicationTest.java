package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidMedicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Medication")
class MedicationTest {

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("crea un medicamento válido activo por defecto")
        void createsValidMedicationActiveByDefault() {
            Medication medication = Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                    DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), "con comidas");

            assertThat(medication.getMedicationId()).isNotNull();
            assertThat(medication.getName()).isEqualTo("Metformina");
            assertThat(medication.isActive()).isTrue();
            assertThat(medication.getEndDate()).isNull();
            assertThat(medication.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        }

        @Test
        @DisplayName("usa la fecha de hoy cuando no se especifica fecha de inicio")
        void usesTodayWhenStartDateIsNull() {
            Medication medication = Medication.create(
                    patientId, "Insulina", MedicationType.INSULIN_BASAL, BigDecimal.valueOf(10),
                    DoseUnit.UNITS, MedicationFrequency.ONCE_DAILY, null, null);

            assertThat(medication.getStartDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("rechaza nombre nulo")
        void rejectsNullName() {
            assertThatThrownBy(() -> Medication.create(
                    patientId, null, MedicationType.ORAL, BigDecimal.valueOf(10),
                    DoseUnit.MG, MedicationFrequency.ONCE_DAILY, null, null))
                    .isInstanceOf(InvalidMedicationException.class)
                    .hasMessageContaining("nombre");
        }

        @Test
        @DisplayName("rechaza nombre en blanco")
        void rejectsBlankName() {
            assertThatThrownBy(() -> Medication.create(
                    patientId, "   ", MedicationType.ORAL, BigDecimal.valueOf(10),
                    DoseUnit.MG, MedicationFrequency.ONCE_DAILY, null, null))
                    .isInstanceOf(InvalidMedicationException.class);
        }

        @Test
        @DisplayName("rechaza dosis nula")
        void rejectsNullDose() {
            assertThatThrownBy(() -> Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, null,
                    DoseUnit.MG, MedicationFrequency.ONCE_DAILY, null, null))
                    .isInstanceOf(InvalidMedicationException.class)
                    .hasMessageContaining("positivo");
        }

        @Test
        @DisplayName("rechaza dosis igual a cero")
        void rejectsZeroDose() {
            assertThatThrownBy(() -> Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, BigDecimal.ZERO,
                    DoseUnit.MG, MedicationFrequency.ONCE_DAILY, null, null))
                    .isInstanceOf(InvalidMedicationException.class);
        }

        @Test
        @DisplayName("rechaza dosis negativa")
        void rejectsNegativeDose() {
            assertThatThrownBy(() -> Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(-5),
                    DoseUnit.MG, MedicationFrequency.ONCE_DAILY, null, null))
                    .isInstanceOf(InvalidMedicationException.class);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("marca el medicamento como inactivo y registra la fecha de fin")
        void marksInactiveAndSetsEndDate() {
            Medication medication = validMedication();

            medication.deactivate();

            assertThat(medication.isActive()).isFalse();
            assertThat(medication.getEndDate()).isEqualTo(LocalDate.now());
        }
    }

    @Nested
    @DisplayName("updateDose")
    class UpdateDose {

        @Test
        @DisplayName("actualiza la dosis y la unidad correctamente")
        void updatesDoseAndUnit() {
            Medication medication = validMedication();

            medication.updateDose(BigDecimal.valueOf(1000), DoseUnit.MG);

            assertThat(medication.getDose()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(medication.getDoseUnit()).isEqualTo(DoseUnit.MG);
        }

        @Test
        @DisplayName("rechaza actualizar a una dosis inválida")
        void rejectsInvalidNewDose() {
            Medication medication = validMedication();

            assertThatThrownBy(() -> medication.updateDose(BigDecimal.ZERO, DoseUnit.MG))
                    .isInstanceOf(InvalidMedicationException.class);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Medication validMedication() {
        return Medication.create(
                patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), null);
    }
}