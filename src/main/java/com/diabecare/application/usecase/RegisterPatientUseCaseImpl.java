package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterPatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterPatientUseCaseImpl implements RegisterPatientUseCase {

    private final SavePatientPort savePatientPort;
    private final LoadPatientPort loadPatientPort;

    @Override
    public Patient execute(Command command) {
        if (loadPatientPort.existsByUserId(command.userId())) {
            throw new InvalidPatientDataException(
                    "Ya existe un perfil de paciente para este usuario");
        }
        Patient patient = Patient.create(
                command.userId(),
                command.fullName(),
                command.dateOfBirth(),
                command.diabetesType(),
                command.diagnosisDate(),
                command.heightCm()
        );
        return savePatientPort.save(patient);
    }
}