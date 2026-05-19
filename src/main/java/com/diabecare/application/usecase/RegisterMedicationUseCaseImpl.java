package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMedicationUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveMedicationPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.Medication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterMedicationUseCaseImpl implements RegisterMedicationUseCase {

    private final SaveMedicationPort saveMedicationPort;
    private final LoadPatientPort loadPatientPort;

    @Override
    public Medication execute(Command command) {
        loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        Medication medication = Medication.create(
                command.patientId(),
                command.name(),
                command.type(),
                command.dose(),
                command.doseUnit(),
                command.frequency(),
                command.startDate(),
                command.notes()
        );
        return saveMedicationPort.save(medication);
    }
}