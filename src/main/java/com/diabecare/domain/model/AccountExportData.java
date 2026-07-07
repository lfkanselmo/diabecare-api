package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agrupa absolutamente todos los datos personales y de salud de una cuenta
 * para el derecho de acceso/portabilidad (ARCO, Ley 1581 de 2012). A
 * diferencia de {@link ReportData} (que resume un rango de fechas para un
 * PDF clínico), esto es un volcado íntegro, sin resúmenes ni filtros.
 */
@Getter
@Builder
public class AccountExportData {

    private String email;
    private LocalDateTime accountCreatedAt;
    private LocalDateTime termsAcceptedAt;
    private String termsVersion;

    private Patient patient;

    private List<GlucoseReading> glucoseReadings;
    private List<MealEntry> mealEntries;
    private List<VitalSign> vitalSigns;
    private List<Medication> medications;
    private List<ExerciseLog> exerciseLogs;
    private List<MenstrualCycle> menstrualCycles;

    private List<CaregiverLink> caregiversWithAccessToMyData;
    private List<CaregiverLink> patientsICareFor;
}
