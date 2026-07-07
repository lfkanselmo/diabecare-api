package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.GlucoseStatus;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.MealType;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.domain.model.SymptomSeverity;
import lombok.RequiredArgsConstructor;

/**
 * Resuelve las etiquetas legibles de los catálogos de metadatos (tipos de medicamento,
 * unidades, frecuencias, etc.) vía {@link MessageResolverPort}, para que respondan al
 * idioma del cliente (header Accept-Language) igual que las alertas y el ciclo menstrual.
 */
@RequiredArgsConstructor
public class MetadataLabelService {

    private final MessageResolverPort messages;

    public String resolveMedicationTypeLabel(MedicationType type) {
        return resolve("medication.type", type.name());
    }

    public String resolveDoseUnitLabel(DoseUnit unit) {
        return resolve("dose.unit", unit.name());
    }

    public String resolveFrequencyLabel(MedicationFrequency frequency) {
        return resolve("medication.frequency", frequency.name());
    }

    public String resolveMealTypeLabel(MealType type) {
        return resolve("meal.type", type.name());
    }

    public String resolveReadingTypeLabel(ReadingType type) {
        return resolve("reading.type", type.name());
    }

    public String resolveActivityLevelLabel(ActivityLevel level) {
        return resolve("activity.level", level.name());
    }

    public String resolveDiabetesTypeLabel(DiabetesType type) {
        return resolve("diabetes.type", type.name());
    }

    public String resolveGlucoseUnitLabel(GlucoseUnit unit) {
        return resolve("glucose.unit", unit.name());
    }

    public String resolveGlucoseStatusLabel(GlucoseStatus status) {
        return resolve("glucose.status", status.name());
    }

    public String resolveSeverityLabel(SymptomSeverity severity) {
        return resolve("symptom.severity", severity.name());
    }

    private String resolve(String prefix, String enumName) {
        String key = prefix + "." + enumName.toLowerCase().replace('_', '-');
        return messages.resolve(key);
    }
}
