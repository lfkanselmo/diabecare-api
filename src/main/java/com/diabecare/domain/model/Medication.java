package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidMedicationException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class Medication {

    private final UUID medicationId;
    private final UUID patientId;
    private String name;
    private MedicationType type;
    private BigDecimal dose;
    private DoseUnit doseUnit;
    private MedicationFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private String notes;

    public static Medication create(
            UUID patientId,
            String name,
            MedicationType type,
            BigDecimal dose,
            DoseUnit doseUnit,
            MedicationFrequency frequency,
            LocalDate startDate,
            String notes
    ) {
        validateName(name);
        validateDose(dose);

        return Medication.builder()
                .medicationId(UUID.randomUUID())
                .patientId(patientId)
                .name(name)
                .type(type)
                .dose(dose)
                .doseUnit(doseUnit)
                .frequency(frequency)
                .startDate(startDate != null ? startDate : LocalDate.now())
                .active(true)
                .notes(notes)
                .build();
    }

    public void deactivate() {
        this.active = false;
        this.endDate = LocalDate.now();
    }

    public void updateDose(BigDecimal newDose, DoseUnit newUnit) {
        validateDose(newDose);
        this.dose = newDose;
        this.doseUnit = newUnit;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidMedicationException("El nombre del medicamento es obligatorio");
        }
    }

    private static void validateDose(BigDecimal dose) {
        if (dose == null || dose.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidMedicationException("La dosis debe ser un valor positivo");
        }
    }
}