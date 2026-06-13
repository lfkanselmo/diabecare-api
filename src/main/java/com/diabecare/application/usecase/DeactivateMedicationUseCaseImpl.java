package com.diabecare.application.usecase;

import com.diabecare.application.port.in.DeactivateMedicationUseCase;
import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SaveMedicationPort;
import com.diabecare.domain.exception.InvalidMedicationException;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DeactivateMedicationUseCaseImpl implements DeactivateMedicationUseCase {

    private final LoadMedicationPort loadMedicationPort;
    private final SaveMedicationPort saveMedicationPort;
    private final SaveAuditLogPort   saveAuditLogPort;
    private final AuditService       auditService;

    @Override
    public void execute(UUID medicationId, UUID patientId) {
        Medication medication = loadMedicationPort.findById(medicationId)
                .orElseThrow(() -> new InvalidMedicationException(
                        "Medicamento no encontrado: " + medicationId));

        if (!medication.getPatientId().equals(patientId)) {
            throw new InvalidMedicationException(
                    "No tienes permisos para modificar este medicamento");
        }

        saveAuditLogPort.save(auditService.buildDeleteLog(
                patientId, "MEDICATION", medicationId
        ));

        medication.deactivate();
        saveMedicationPort.save(medication);
    }
}