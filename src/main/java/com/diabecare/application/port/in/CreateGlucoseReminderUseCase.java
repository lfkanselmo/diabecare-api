package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReminder;

import java.time.LocalTime;
import java.util.UUID;

public interface CreateGlucoseReminderUseCase {

    record Command(UUID patientId, LocalTime reminderTime, String label) {}

    GlucoseReminder execute(Command command);
}
