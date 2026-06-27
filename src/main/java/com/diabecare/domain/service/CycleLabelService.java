package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.CycleSymptom;
import com.diabecare.domain.model.FlowIntensity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CycleLabelService {

    private final MessageResolverPort messages;

    public String resolveFlowLabel(FlowIntensity intensity) {
        String key = switch (intensity) {
            case NONE -> "cycle.flow.none";
            case SPOTTING -> "cycle.flow.spotting";
            case LIGHT -> "cycle.flow.light";
            case MODERATE -> "cycle.flow.moderate";
            case HEAVY -> "cycle.flow.heavy";
            case VERY_HEAVY -> "cycle.flow.very-heavy";
        };
        return messages.resolve(key);
    }

    public String resolveSymptomLabel(CycleSymptom symptom) {
        String key = "cycle.symptom." + symptom.name().toLowerCase().replace('_', '-');
        return messages.resolve(key);
    }
}