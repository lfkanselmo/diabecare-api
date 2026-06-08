package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UpdatePatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdatePatientUseCaseImpl implements UpdatePatientUseCase {

    private final LoadPatientPort loadPatientPort;
    private final SavePatientPort savePatientPort;

    @Override
    public Patient execute(Command command) {
        Patient patient = loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        patient.updateGlucoseTarget(command.targetGlucoseMin(), command.targetGlucoseMax());
        patient.updateDailyCalorieGoal(command.dailyCalorieGoal());
        patient.updateActivityLevel(command.activityLevel());
        patient.updatePreferredGlucoseUnit(command.preferredGlucoseUnit());

        return savePatientPort.save(patient);
    }
}