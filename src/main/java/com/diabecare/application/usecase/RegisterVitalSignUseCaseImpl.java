package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterVitalSignUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveVitalSignPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.VitalSign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterVitalSignUseCaseImpl implements RegisterVitalSignUseCase {

    private final SaveVitalSignPort saveVitalSignPort;
    private final LoadPatientPort loadPatientPort;

    @Override
    public VitalSign execute(Command command) {
        loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        VitalSign vitalSign = VitalSign.create(
                command.patientId(),
                command.weightKg(),
                command.heightCm(),
                command.systolicBp(),
                command.diastolicBp(),
                command.heartRate(),
                command.hba1c(),
                command.measuredAt(),
                command.notes()
        );
        return saveVitalSignPort.save(vitalSign);
    }
}