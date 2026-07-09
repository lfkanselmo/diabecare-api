package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
public class GlucoseReminder {

    private UUID          id;
    private UUID          patientId;
    private LocalTime     reminderTime;
    private String        label;
    private boolean       enabled;
    private LocalDateTime createdAt;
}
