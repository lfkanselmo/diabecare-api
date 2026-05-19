package com.diabecare.application.usecase;

import com.diabecare.application.port.in.DeactivateMedicationUseCase;
import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.application.port.out.SaveMedicationPort;
import com.diabecare.domain.exception.InvalidMedicationException;
import com.diabecare.domain.model.Medication;
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

    @Override
    public void execute(UUID medicationId, UUID patientId) {
        Medication medication = loadMedicationPort.findById(medicationId)
                .orElseThrow(() -> new InvalidMedicationException(
                        "Medicamento no encontrado: " + medicationId));

        if (!medication.getPatientId().equals(patientId)) {
            throw new InvalidMedicationException(
                    "No tienes permisos para modificar este medicamento");
        }
        medication.deactivate();
        saveMedicationPort.save(medication);
    }
}