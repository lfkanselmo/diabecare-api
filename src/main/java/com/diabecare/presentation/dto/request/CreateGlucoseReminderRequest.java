package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record CreateGlucoseReminderRequest(
        @NotNull
        LocalTime reminderTime,

        @Size(max = 50)
        String label
) {}
