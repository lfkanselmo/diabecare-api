package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UpdatePatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdatePatientUseCaseImpl implements UpdatePatientUseCase {

    private final LoadPatientPort loadPatientPort;
    private final SavePatientPort savePatientPort;
    private final SaveAuditLogPort saveAuditLogPort;
    private final AuditService     auditService;

    @Override
    public Patient execute(Command command) {
        Patient patient = loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        auditHeight(patient, command);
        auditGlucoseTarget(patient, command);
        auditCalorieGoal(patient, command);
        auditActivityLevel(patient, command);
        auditGlucoseUnit(patient, command);

        if (command.heightCm() != null) {
            patient.updateHeight(command.heightCm());
        }
        patient.updateGlucoseTarget(command.targetGlucoseMin(), command.targetGlucoseMax());
        patient.updateDailyCalorieGoal(command.dailyCalorieGoal());
        patient.updateActivityLevel(command.activityLevel());
        patient.updatePreferredGlucoseUnit(command.preferredGlucoseUnit());

        return savePatientPort.save(patient);
    }

    private void auditHeight(Patient patient, Command command) {
        if (command.heightCm() == null) return;
        String oldVal = patient.getHeightCm() != null
                ? patient.getHeightCm().toPlainString() : "null";
        String newVal = command.heightCm().toPlainString();

        if (!oldVal.equals(newVal)) {
            saveAuditLogPort.save(auditService.buildUpdateLog(
                    patient.getPatientId(), "PATIENT", patient.getPatientId(),
                    "heightCm", oldVal, newVal
            ));
        }
    }

    private void auditGlucoseTarget(Patient patient, Command command) {
        String oldMin = patient.getTargetGlucoseMin().toPlainString();
        String oldMax = patient.getTargetGlucoseMax().toPlainString();
        String newMin = command.targetGlucoseMin().toPlainString();
        String newMax = command.targetGlucoseMax().toPlainString();

        if (!oldMin.equals(newMin) || !oldMax.equals(newMax)) {
            saveAuditLogPort.save(auditService.buildUpdateLog(
                    patient.getPatientId(), "PATIENT", patient.getPatientId(),
                    "targetGlucoseRange",
                    oldMin + "-" + oldMax,
                    newMin + "-" + newMax
            ));
        }
    }

    private void auditCalorieGoal(Patient patient, Command command) {
        if (command.dailyCalorieGoal() == null) return;
        String oldVal = patient.getDailyCalorieGoal() != null
                ? patient.getDailyCalorieGoal().toString() : "null";
        String newVal = command.dailyCalorieGoal().toString();

        if (!oldVal.equals(newVal)) {
            saveAuditLogPort.save(auditService.buildUpdateLog(
                    patient.getPatientId(), "PATIENT", patient.getPatientId(),
                    "dailyCalorieGoal", oldVal, newVal
            ));
        }
    }

    private void auditActivityLevel(Patient patient, Command command) {
        if (command.activityLevel() == null) return;
        String oldVal = patient.getActivityLevel() != null
                ? patient.getActivityLevel().name() : "null";
        String newVal = command.activityLevel().name();

        if (!oldVal.equals(newVal)) {
            saveAuditLogPort.save(auditService.buildUpdateLog(
                    patient.getPatientId(), "PATIENT", patient.getPatientId(),
                    "activityLevel", oldVal, newVal
            ));
        }
    }

    private void auditGlucoseUnit(Patient patient, Command command) {
        if (command.preferredGlucoseUnit() == null) return;
        String oldVal = patient.getPreferredGlucoseUnit() != null
                ? patient.getPreferredGlucoseUnit().name() : "null";
        String newVal = command.preferredGlucoseUnit().name();

        if (!oldVal.equals(newVal)) {
            saveAuditLogPort.save(auditService.buildUpdateLog(
                    patient.getPatientId(), "PATIENT", patient.getPatientId(),
                    "preferredGlucoseUnit", oldVal, newVal
            ));
        }
    }
}