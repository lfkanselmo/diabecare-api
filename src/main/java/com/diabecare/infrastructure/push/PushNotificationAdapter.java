package com.diabecare.infrastructure.push;

import com.diabecare.application.port.out.NotifyPatientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PushNotificationAdapter implements NotifyPatientPort {

    private final PushNotificationService pushNotificationService;

    @Override
    public void notify(UUID patientId, String title, String message) {
        pushNotificationService.sendToPatient(patientId, title, message);
    }
}