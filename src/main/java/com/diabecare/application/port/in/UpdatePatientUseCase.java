package com.diabecare.application.port.in;

import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.Patient;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdatePatientUseCase {

    record Command(
            UUID patientId,
            BigDecimal heightCm,
            BigDecimal targetGlucoseMin,
            BigDecimal targetGlucoseMax,
            Integer dailyCalorieGoal,
            ActivityLevel activityLevel,
            GlucoseUnit preferredGlucoseUnit
    ) {}

    Patient execute(Command command);
}