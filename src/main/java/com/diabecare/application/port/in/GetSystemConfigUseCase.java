package com.diabecare.application.port.in;

import com.diabecare.domain.model.SystemConfig;
import java.util.List;

public interface GetSystemConfigUseCase {
    List<SystemConfig> getAll();
    void reload();
}