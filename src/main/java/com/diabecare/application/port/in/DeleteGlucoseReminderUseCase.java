package com.diabecare.application.port.in;

import java.util.UUID;

public interface DeleteGlucoseReminderUseCase {
    void execute(UUID patientId, UUID reminderId);
}
