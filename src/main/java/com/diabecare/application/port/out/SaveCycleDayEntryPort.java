package com.diabecare.application.port.out;

import com.diabecare.domain.model.CycleDayEntry;

public interface SaveCycleDayEntryPort {
    CycleDayEntry save(CycleDayEntry entry);
}