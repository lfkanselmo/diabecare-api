package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CycleSymptomEntry {
    private CycleSymptom symptom;
    private SymptomSeverity severity;
}