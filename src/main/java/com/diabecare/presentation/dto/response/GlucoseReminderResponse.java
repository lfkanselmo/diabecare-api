package com.diabecare.presentation.dto.response;

import java.time.LocalTime;
import java.util.UUID;

public record GlucoseReminderResponse(
        UUID id,
        LocalTime reminderTime,
        String label,
        boolean enabled
) {}
