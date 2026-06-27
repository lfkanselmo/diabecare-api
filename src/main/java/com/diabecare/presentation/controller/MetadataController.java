package com.diabecare.presentation.controller;

import com.diabecare.domain.model.*;
import com.diabecare.domain.service.CycleLabelService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final CycleLabelService cycleLabelService;
    private final MenstrualCycleGuidanceService cycleGuidanceService;

    @GetMapping("/exercise-types")
    public ResponseEntity<List<Map<String, String>>> getExerciseTypes() {
        return ResponseEntity.ok(Arrays.stream(ExerciseType.values())
                .map(e -> Map.of("value", e.name(), "label", getExerciseLabel(e)))
                .toList());
    }

    @GetMapping("/exercise-intensities")
    public ResponseEntity<List<Map<String, String>>> getExerciseIntensities() {
        return ResponseEntity.ok(Arrays.stream(ExerciseIntensity.values())
                .map(i -> Map.of("value", i.name(), "label", getIntensityLabel(i)))
                .toList());
    }

    @GetMapping("/medication-types")
    public ResponseEntity<List<Map<String, String>>> getMedicationTypes() {
        return ResponseEntity.ok(Arrays.stream(MedicationType.values())
                .map(t -> Map.of("value", t.name(), "label", getMedicationTypeLabel(t)))
                .toList());
    }

    @GetMapping("/dose-units")
    public ResponseEntity<List<Map<String, String>>> getDoseUnits() {
        return ResponseEntity.ok(Arrays.stream(DoseUnit.values())
                .map(u -> Map.of("value", u.name(), "label", getDoseUnitLabel(u)))
                .toList());
    }

    @GetMapping("/medication-frequencies")
    public ResponseEntity<List<Map<String, String>>> getMedicationFrequencies() {
        return ResponseEntity.ok(Arrays.stream(MedicationFrequency.values())
                .map(f -> Map.of("value", f.name(), "label", getFrequencyLabel(f)))
                .toList());
    }

    @GetMapping("/meal-types")
    public ResponseEntity<List<Map<String, String>>> getMealTypes() {
        return ResponseEntity.ok(Arrays.stream(MealType.values())
                .map(m -> Map.of("value", m.name(), "label", getMealTypeLabel(m)))
                .toList());
    }

    @GetMapping("/reading-types")
    public ResponseEntity<List<Map<String, String>>> getReadingTypes() {
        return ResponseEntity.ok(Arrays.stream(ReadingType.values())
                .map(r -> Map.of("value", r.name(), "label", getReadingTypeLabel(r)))
                .toList());
    }

    @GetMapping("/activity-levels")
    public ResponseEntity<List<Map<String, String>>> getActivityLevels() {
        return ResponseEntity.ok(Arrays.stream(ActivityLevel.values())
                .map(a -> Map.of("value", a.name(), "label", getActivityLevelLabel(a)))
                .toList());
    }

    @GetMapping("/diabetes-types")
    public ResponseEntity<List<Map<String, String>>> getDiabetesTypes() {
        return ResponseEntity.ok(Arrays.stream(DiabetesType.values())
                .map(d -> Map.of("value", d.name(), "label", getDiabetesTypeLabel(d)))
                .toList());
    }

    @GetMapping("/glucose-units")
    public ResponseEntity<List<Map<String, String>>> getGlucoseUnits() {
        return ResponseEntity.ok(Arrays.stream(GlucoseUnit.values())
                .map(g -> Map.of("value", g.name(), "label", getGlucoseUnitLabel(g)))
                .toList());
    }

    @GetMapping("/glucose-statuses")
    public ResponseEntity<List<Map<String, String>>> getGlucoseStatuses() {
        return ResponseEntity.ok(Arrays.stream(GlucoseStatus.values())
                .map(s -> Map.of("value", s.name(), "label", getGlucoseStatusLabel(s)))
                .toList());
    }

    @GetMapping("/cycle-symptoms")
    public ResponseEntity<List<Map<String, String>>> getCycleSymptoms() {
        return ResponseEntity.ok(Arrays.stream(CycleSymptom.values())
                .map(s -> Map.of("value", s.name(), "label", cycleLabelService.resolveSymptomLabel(s)))
                .toList());
    }

    @GetMapping("/flow-intensities")
    public ResponseEntity<List<Map<String, String>>> getFlowIntensities() {
        return ResponseEntity.ok(Arrays.stream(FlowIntensity.values())
                .map(f -> Map.of("value", f.name(), "label", cycleLabelService.resolveFlowLabel(f)))
                .toList());
    }

    @GetMapping("/symptom-severities")
    public ResponseEntity<List<Map<String, String>>> getSymptomSeverities() {
        return ResponseEntity.ok(Arrays.stream(SymptomSeverity.values())
                .map(s -> Map.of("value", s.name(), "label", getSeverityLabel(s)))
                .toList());
    }

    @GetMapping("/cycle-phases")
    public ResponseEntity<List<Map<String, String>>> getCyclePhases() {
        return ResponseEntity.ok(Arrays.stream(CyclePhase.values())
                .map(p -> Map.of("value", p.name(), "label", cycleGuidanceService.resolveLabel(p)))
                .toList());
    }

    // ── Labels ────────────────────────────────────────────────────────────────

    private String getExerciseLabel(ExerciseType type) {
        return switch (type) {
            case WALKING         -> "Caminata";
            case RUNNING         -> "Trote / Carrera";
            case CYCLING         -> "Ciclismo";
            case SWIMMING        -> "Natación";
            case WEIGHT_TRAINING -> "Pesas";
            case YOGA            -> "Yoga";
            case FOOTBALL        -> "Fútbol";
            case BASKETBALL      -> "Baloncesto";
            case DANCING         -> "Baile";
            case HIKING          -> "Senderismo";
            case OTHER           -> "Otro";
        };
    }

    private String getIntensityLabel(ExerciseIntensity intensity) {
        return switch (intensity) {
            case LOW      -> "Baja";
            case MODERATE -> "Moderada";
            case HIGH     -> "Alta";
        };
    }

    private String getMedicationTypeLabel(MedicationType type) {
        return switch (type) {
            case INSULIN_BASAL -> "Insulina basal";
            case INSULIN_BOLUS -> "Insulina bolo";
            case ORAL          -> "Medicamento oral";
            case INJECTABLE    -> "Inyectable";
        };
    }

    private String getDoseUnitLabel(DoseUnit unit) {
        return switch (unit) {
            case MG    -> "mg";
            case ML    -> "mL";
            case UNITS -> "Unidades";
        };
    }

    private String getFrequencyLabel(MedicationFrequency frequency) {
        return switch (frequency) {
            case ONCE_DAILY        -> "Una vez al día";
            case TWICE_DAILY       -> "Dos veces al día";
            case THREE_TIMES_DAILY -> "Tres veces al día";
            case WITH_MEALS        -> "Con las comidas";
            case BEFORE_MEALS      -> "Antes de comidas";
            case AT_BEDTIME        -> "Al acostarse";
            case AS_NEEDED         -> "Según necesidad";
        };
    }

    private String getMealTypeLabel(MealType type) {
        return switch (type) {
            case BREAKFAST -> "Desayuno";
            case LUNCH     -> "Almuerzo";
            case DINNER    -> "Cena";
            case SNACK     -> "Merienda";
        };
    }

    private String getReadingTypeLabel(ReadingType type) {
        return switch (type) {
            case FASTING   -> "Ayuno";
            case PRE_MEAL  -> "Antes de comer";
            case POST_MEAL -> "Después de comer";
            case BEDTIME   -> "Antes de dormir";
            case RANDOM    -> "En cualquier momento";
        };
    }

    private String getActivityLevelLabel(ActivityLevel level) {
        return switch (level) {
            case SEDENTARY         -> "Sedentario";
            case LIGHTLY_ACTIVE    -> "Ligeramente activo";
            case MODERATELY_ACTIVE -> "Moderadamente activo";
            case VERY_ACTIVE       -> "Muy activo";
        };
    }

    private String getDiabetesTypeLabel(DiabetesType type) {
        return switch (type) {
            case TYPE_1      -> "Tipo 1";
            case TYPE_2      -> "Tipo 2";
            case GESTATIONAL -> "Gestacional";
            case LADA        -> "LADA";
            case MODY        -> "MODY";
        };
    }

    private String getGlucoseUnitLabel(GlucoseUnit unit) {
        return switch (unit) {
            case MG_DL  -> "mg/dL";
            case MMOL_L -> "mmol/L";
        };
    }

    private String getGlucoseStatusLabel(GlucoseStatus status) {
        return switch (status) {
            case CRITICALLY_LOW  -> "Crítico bajo";
            case LOW             -> "Bajo";
            case NORMAL          -> "Normal";
            case HIGH            -> "Alto";
            case CRITICALLY_HIGH -> "Crítico alto";
        };
    }

    private String getSeverityLabel(SymptomSeverity severity) {
        return switch (severity) {
            case MILD     -> "Leve";
            case MODERATE -> "Moderado";
            case SEVERE   -> "Severo";
        };
    }
}