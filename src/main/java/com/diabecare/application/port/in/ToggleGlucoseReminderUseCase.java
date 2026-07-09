package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReminder;

import java.util.UUID;

public interface ToggleGlucoseReminderUseCase {
    GlucoseReminder execute(UUID patientId, UUID reminderId, boolean enabled);
}
