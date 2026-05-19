package com.diabecare.application.port.out;

import com.diabecare.domain.model.VitalSign;

public interface SaveVitalSignPort {
    VitalSign save(VitalSign vitalSign);
}