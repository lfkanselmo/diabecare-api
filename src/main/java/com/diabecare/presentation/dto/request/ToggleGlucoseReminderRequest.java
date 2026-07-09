package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record ToggleGlucoseReminderRequest(
        @NotNull
        Boolean enabled
) {}
