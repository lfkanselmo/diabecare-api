package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidMedicationException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    // Solo poblado al leer desde persistencia — es el cursor que usa el motor
    // de sync offline del móvil, no un dato de dominio que se establezca al
    // crear un medicamento nuevo (mismo patrón que GlucoseReading.updatedAt).
    private LocalDateTime updatedAt;

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
        return createWithId(UUID.randomUUID(), patientId, name, type, dose, doseUnit, frequency, startDate, notes);
    }

    /**
     * Igual que {@link #create}, honrando un ID provisto por el cliente — ver
     * {@link GlucoseReading#createWithId}.
     */
    public static Medication createWithId(
            UUID medicationId,
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
                .medicationId(medicationId)
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