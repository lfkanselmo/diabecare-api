package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExerciseLabelService {

    private final MessageResolverPort messages;

    public String resolveTypeLabel(ExerciseType type) {
        return resolve("exercise.type", type.name());
    }

    public String resolveIntensityLabel(ExerciseIntensity intensity) {
        return resolve("exercise.intensity", intensity.name());
    }

    private String resolve(String prefix, String enumName) {
        String key = prefix + "." + enumName.toLowerCase().replace('_', '-');
        return messages.resolve(key);
    }
}
