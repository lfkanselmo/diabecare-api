package com.diabecare.application.port.out;

import java.util.UUID;

public interface NotifyPatientPort {
    void notify(UUID patientId, String title, String message);
}