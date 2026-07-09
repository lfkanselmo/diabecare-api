package com.diabecare.application.port.out;

import com.diabecare.domain.model.GlucoseReminder;

import java.util.UUID;

public interface SaveGlucoseReminderPort {
    GlucoseReminder save(GlucoseReminder reminder);
    void delete(UUID reminderId);
}
