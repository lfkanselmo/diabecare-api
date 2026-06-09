package com.diabecare.application.port.out;

import com.diabecare.domain.model.MenstrualCycle;

public interface SaveMenstrualCyclePort {
    MenstrualCycle save(MenstrualCycle cycle);
}