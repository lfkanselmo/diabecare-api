package com.diabecare.presentation.controller;

import com.diabecare.domain.model.*;
import com.diabecare.domain.service.CycleLabelService;
import com.diabecare.domain.service.ExerciseLabelService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.diabecare.domain.service.MetadataLabelService;
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
    private final ExerciseLabelService exerciseLabelService;
    private final MetadataLabelService metadataLabelService;

    @GetMapping("/exercise-types")
    public ResponseEntity<List<Map<String, String>>> getExerciseTypes() {
        return ResponseEntity.ok(Arrays.stream(ExerciseType.values())
                .map(e -> Map.of("value", e.name(), "label", exerciseLabelService.resolveTypeLabel(e)))
                .toList());
    }

    @GetMapping("/exercise-intensities")
    public ResponseEntity<List<Map<String, String>>> getExerciseIntensities() {
        return ResponseEntity.ok(Arrays.stream(ExerciseIntensity.values())
                .map(i -> Map.of("value", i.name(), "label", exerciseLabelService.resolveIntensityLabel(i)))
                .toList());
    }

    @GetMapping("/medication-types")
    public ResponseEntity<List<Map<String, String>>> getMedicationTypes() {
        return ResponseEntity.ok(Arrays.stream(MedicationType.values())
                .map(t -> Map.of("value", t.name(), "label", metadataLabelService.resolveMedicationTypeLabel(t)))
                .toList());
    }

    @GetMapping("/dose-units")
    public ResponseEntity<List<Map<String, String>>> getDoseUnits() {
        return ResponseEntity.ok(Arrays.stream(DoseUnit.values())
                .map(u -> Map.of("value", u.name(), "label", metadataLabelService.resolveDoseUnitLabel(u)))
                .toList());
    }

    @GetMapping("/medication-frequencies")
    public ResponseEntity<List<Map<String, String>>> getMedicationFrequencies() {
        return ResponseEntity.ok(Arrays.stream(MedicationFrequency.values())
                .map(f -> Map.of("value", f.name(), "label", metadataLabelService.resolveFrequencyLabel(f)))
                .toList());
    }

    @GetMapping("/meal-types")
    public ResponseEntity<List<Map<String, String>>> getMealTypes() {
        return ResponseEntity.ok(Arrays.stream(MealType.values())
                .map(m -> Map.of("value", m.name(), "label", metadataLabelService.resolveMealTypeLabel(m)))
                .toList());
    }

    @GetMapping("/reading-types")
    public ResponseEntity<List<Map<String, String>>> getReadingTypes() {
        return ResponseEntity.ok(Arrays.stream(ReadingType.values())
                .map(r -> Map.of("value", r.name(), "label", metadataLabelService.resolveReadingTypeLabel(r)))
                .toList());
    }

    @GetMapping("/activity-levels")
    public ResponseEntity<List<Map<String, String>>> getActivityLevels() {
        return ResponseEntity.ok(Arrays.stream(ActivityLevel.values())
                .map(a -> Map.of("value", a.name(), "label", metadataLabelService.resolveActivityLevelLabel(a)))
                .toList());
    }

    @GetMapping("/diabetes-types")
    public ResponseEntity<List<Map<String, String>>> getDiabetesTypes() {
        return ResponseEntity.ok(Arrays.stream(DiabetesType.values())
                .map(d -> Map.of("value", d.name(), "label", metadataLabelService.resolveDiabetesTypeLabel(d)))
                .toList());
    }

    @GetMapping("/glucose-units")
    public ResponseEntity<List<Map<String, String>>> getGlucoseUnits() {
        return ResponseEntity.ok(Arrays.stream(GlucoseUnit.values())
                .map(g -> Map.of("value", g.name(), "label", metadataLabelService.resolveGlucoseUnitLabel(g)))
                .toList());
    }

    @GetMapping("/glucose-statuses")
    public ResponseEntity<List<Map<String, String>>> getGlucoseStatuses() {
        return ResponseEntity.ok(Arrays.stream(GlucoseStatus.values())
                .map(s -> Map.of("value", s.name(), "label", metadataLabelService.resolveGlucoseStatusLabel(s)))
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
                .map(s -> Map.of("value", s.name(), "label", metadataLabelService.resolveSeverityLabel(s)))
                .toList());
    }

    @GetMapping("/cycle-phases")
    public ResponseEntity<List<Map<String, String>>> getCyclePhases() {
        return ResponseEntity.ok(Arrays.stream(CyclePhase.values())
                .map(p -> Map.of("value", p.name(), "label", cycleGuidanceService.resolveLabel(p)))
                .toList());
    }
}
