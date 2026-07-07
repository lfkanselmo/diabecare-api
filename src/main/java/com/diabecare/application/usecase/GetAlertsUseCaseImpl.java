package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetAlertsUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.Alert;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.AlertDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAlertsUseCaseImpl implements GetAlertsUseCase {

    private final LoadPatientPort loadPatientPort;
    private final List<AlertDetector> alertDetectors;

    @Override
    public List<Alert> getAlerts(UUID patientId) {
        Patient patient = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));

        LocalDateTime now = LocalDateTime.now();

        return alertDetectors.stream()
                .flatMap(detector -> detector.detect(patient, now).stream())
                .toList();
    }
}
