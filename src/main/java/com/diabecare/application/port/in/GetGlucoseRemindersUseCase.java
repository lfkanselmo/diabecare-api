package com.diabecare.application.port.in;

import com.diabecare.domain.model.GlucoseReminder;

import java.util.List;
import java.util.UUID;

public interface GetGlucoseRemindersUseCase {
    List<GlucoseReminder> execute(UUID patientId);
}
