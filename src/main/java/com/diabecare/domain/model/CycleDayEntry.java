package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CycleDayEntry {

    private final UUID dayEntryId;
    private final UUID cycleId;
    private final UUID patientId;
    private LocalDate entryDate;
    private FlowIntensity flowIntensity;
    private String notes;
    @Builder.Default
    private List<CycleSymptomEntry> symptoms = Collections.emptyList();

    public static CycleDayEntry create(UUID cycleId, UUID patientId, LocalDate entryDate,
                                       FlowIntensity flowIntensity, String notes,
                                       List<CycleSymptomEntry> symptoms) {
        return CycleDayEntry.builder()
                .dayEntryId(UUID.randomUUID())
                .cycleId(cycleId)
                .patientId(patientId)
                .entryDate(entryDate)
                .flowIntensity(flowIntensity != null ? flowIntensity : FlowIntensity.NONE)
                .notes(notes)
                .symptoms(symptoms != null ? symptoms : Collections.emptyList())
                .build();
    }

    public List<CycleSymptomEntry> getSymptoms() {
        return Collections.unmodifiableList(symptoms);
    }
}