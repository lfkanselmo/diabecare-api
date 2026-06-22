package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.CyclePhase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MenstrualCycleGuidanceService {

    private final MessageResolverPort messages;

    public String resolveGuidance(CyclePhase phase) {
        return messages.resolve(guidanceKey(phase));
    }

    public String resolveLabel(CyclePhase phase) {
        return messages.resolve(labelKey(phase));
    }

    private String guidanceKey(CyclePhase phase) {
        return switch (phase) {
            case MENSTRUATION -> "cycle.guidance.menstruation";
            case FOLLICULAR -> "cycle.guidance.follicular";
            case OVULATION -> "cycle.guidance.ovulation";
            case LUTEAL_EARLY -> "cycle.guidance.luteal-early";
            case LUTEAL_LATE -> "cycle.guidance.luteal-late";
        };
    }

    private String labelKey(CyclePhase phase) {
        return switch (phase) {
            case MENSTRUATION -> "cycle.label.menstruation";
            case FOLLICULAR -> "cycle.label.follicular";
            case OVULATION -> "cycle.label.ovulation";
            case LUTEAL_EARLY -> "cycle.label.luteal-early";
            case LUTEAL_LATE -> "cycle.label.luteal-late";
        };
    }
}