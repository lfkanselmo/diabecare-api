package com.diabecare.domain.service;

import com.diabecare.domain.model.Alert;
import com.diabecare.domain.model.Patient;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Un tipo de alerta clínica autocontenido. Agregar un nuevo tipo de alerta significa
 * crear una nueva implementación y registrarla en {@code AlertDetectorConfig} — nunca
 * modificar {@link com.diabecare.application.usecase.GetAlertsUseCaseImpl} ni las demás
 * implementaciones (principio de abierto/cerrado).
 */
public interface AlertDetector {
    List<Alert> detect(Patient patient, LocalDateTime now);
}
