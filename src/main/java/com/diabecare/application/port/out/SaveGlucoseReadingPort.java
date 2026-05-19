package com.diabecare.application.port.out;

import com.diabecare.domain.model.GlucoseReading;

public interface SaveGlucoseReadingPort {
    GlucoseReading save(GlucoseReading reading);
}