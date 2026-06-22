package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UpdateInsulinProfileUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateInsulinProfileUseCaseImpl implements UpdateInsulinProfileUseCase {

    private final LoadPatientPort loadPatientPort;
    private final SavePatientPort savePatientPort;
    private final SaveAuditLogPort saveAuditLogPort;
    private final AuditService auditService;

    @Override
    public Patient execute(Command command) {
        Patient patient = loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        auditField(patient, "insulinSensitivityFactor",
                patient.getInsulinSensitivityFactor(), command.sensitivityFactor());
        auditField(patient, "insulinToCarbRatio",
                patient.getInsulinToCarbRatio(), command.carbRatio());
        auditField(patient, "targetGlucoseForCorrection",
                patient.getTargetGlucoseForCorrection(), command.targetGlucose());

        patient.updateInsulinProfile(
                command.sensitivityFactor(),
                command.carbRatio(),
                command.targetGlucose());

        return savePatientPort.save(patient);
    }

    private void auditField(Patient patient, String fieldName,
                            BigDecimal oldValue, BigDecimal newValue) {
        String oldStr = oldValue != null ? oldValue.toPlainString() : "null";
        String newStr = newValue != null ? newValue.toPlainString() : "null";

        if (!oldStr.equals(newStr)) {
            saveAuditLogPort.save(auditService.buildUpdateLog(
                    patient.getPatientId(), "PATIENT", patient.getPatientId(),
                    fieldName, oldStr, newStr
            ));
        }
    }
}