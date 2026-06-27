package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMenstrualCycleUseCase;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveMenstrualCyclePort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.exception.OpenCycleConflictException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterMenstrualCycleUseCaseImpl implements RegisterMenstrualCycleUseCase {

    private final SaveMenstrualCyclePort saveMenstrualCyclePort;
    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final LoadPatientPort loadPatientPort;

    @Override
    public MenstrualCycle execute(Command command) {
        Patient patient = loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        if (!patient.isFemale()) {
            throw new InvalidPatientDataException(
                    "El seguimiento del ciclo menstrual solo está disponible " +
                            "para pacientes de sexo femenino.");
        }

        loadMenstrualCyclePort.findLatestByPatientId(command.patientId())
                .filter(MenstrualCycle::isOngoing)
                .ifPresent(ongoing -> {
                    throw new OpenCycleConflictException(ongoing.getStartDate());
                });

        MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                command.patientId(),
                command.startDate(),
                command.notes()
        );

        return saveMenstrualCyclePort.save(cycle);
    }
}