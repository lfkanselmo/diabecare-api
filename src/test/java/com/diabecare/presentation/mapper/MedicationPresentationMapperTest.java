package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.presentation.dto.response.MedicationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MedicationPresentationMapper")
class MedicationPresentationMapperTest {

    private final MedicationPresentationMapper mapper = new MedicationPresentationMapperImpl();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("convierte type, doseUnit y frequency a String correctamente")
        void convertsTypeDoseUnitAndFrequencyToString() {
            Medication medication = Medication.create(
                    UUID.randomUUID(), "Insulina", MedicationType.INSULIN_BASAL, BigDecimal.valueOf(10),
                    DoseUnit.UNITS, MedicationFrequency.AS_NEEDED, LocalDate.of(2026, 1, 1), "antes de comidas");

            MedicationResponse response = mapper.toResponse(medication);

            assertThat(response.type()).isEqualTo("INSULIN_BASAL");
            assertThat(response.doseUnit()).isEqualTo("UNITS");
            assertThat(response.frequency()).isEqualTo("AS_NEEDED");
            assertThat(response.active()).isTrue();
            assertThat(response.notes()).isEqualTo("antes de comidas");
        }

        @Test
        @DisplayName("mapea active como false para un medicamento desactivado")
        void mapsActiveAsFalseForDeactivatedMedication() {
            Medication medication = Medication.create(
                    UUID.randomUUID(), "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                    DoseUnit.MG, MedicationFrequency.ONCE_DAILY, LocalDate.of(2026, 1, 1), null);
            medication.deactivate();

            MedicationResponse response = mapper.toResponse(medication);

            assertThat(response.active()).isFalse();
        }
    }
}