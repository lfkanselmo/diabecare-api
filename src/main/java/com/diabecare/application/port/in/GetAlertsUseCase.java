package com.diabecare.application.port.in;

import com.diabecare.domain.model.Alert;

import java.util.List;
import java.util.UUID;

public interface GetAlertsUseCase {
    List<Alert> getAlerts(UUID patientId);
}