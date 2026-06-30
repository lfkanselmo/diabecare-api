package com.diabecare.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CycleDayEntry")
class CycleDayEntryTest {

    private final UUID cycleId = UUID.randomUUID();
    private final UUID patientId = UUID.randomUUID();
    private final LocalDate entryDate = LocalDate.of(2026, 6, 1);

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("crea un registro válido con todos los campos especificados")
        void createsValidEntryWithAllFields() {
            CycleSymptomEntry symptom = CycleSymptomEntry.builder()
                    .symptom(CycleSymptom.CRAMPS)
                    .severity(SymptomSeverity.MODERATE)
                    .build();

            CycleDayEntry entry = CycleDayEntry.create(
                    cycleId, patientId, entryDate, FlowIntensity.MODERATE, "nota", List.of(symptom));

            assertThat(entry.getDayEntryId()).isNotNull();
            assertThat(entry.getCycleId()).isEqualTo(cycleId);
            assertThat(entry.getPatientId()).isEqualTo(patientId);
            assertThat(entry.getEntryDate()).isEqualTo(entryDate);
            assertThat(entry.getFlowIntensity()).isEqualTo(FlowIntensity.MODERATE);
            assertThat(entry.getNotes()).isEqualTo("nota");
            assertThat(entry.getSymptoms()).hasSize(1).contains(symptom);
        }

        @Test
        @DisplayName("usa NONE como intensidad de flujo por defecto cuando es nula")
        void defaultsFlowIntensityToNone() {
            CycleDayEntry entry = CycleDayEntry.create(
                    cycleId, patientId, entryDate, null, null, null);

            assertThat(entry.getFlowIntensity()).isEqualTo(FlowIntensity.NONE);
        }

        @Test
        @DisplayName("usa una lista vacía de síntomas por defecto cuando es nula")
        void defaultsSymptomsToEmptyList() {
            CycleDayEntry entry = CycleDayEntry.create(
                    cycleId, patientId, entryDate, FlowIntensity.LIGHT, null, null);

            assertThat(entry.getSymptoms()).isEmpty();
        }

        @Test
        @DisplayName("la lista de síntomas expuesta es inmutable")
        void exposedSymptomsListIsImmutable() {
            CycleDayEntry entry = CycleDayEntry.create(
                    cycleId, patientId, entryDate, FlowIntensity.LIGHT, null,
                    List.of(CycleSymptomEntry.builder()
                            .symptom(CycleSymptom.HEADACHE)
                            .severity(SymptomSeverity.MILD)
                            .build()));

            assertThatThrownBy(() -> entry.getSymptoms().add(
                    CycleSymptomEntry.builder()
                            .symptom(CycleSymptom.BLOATING)
                            .severity(SymptomSeverity.MILD)
                            .build()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}