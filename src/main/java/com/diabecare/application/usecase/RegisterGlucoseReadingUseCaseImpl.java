package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterGlucoseReadingUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveGlucoseReadingPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.GlucoseReading;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterGlucoseReadingUseCaseImpl implements RegisterGlucoseReadingUseCase {

    private final SaveGlucoseReadingPort saveGlucoseReadingPort;
    private final LoadPatientPort loadPatientPort;

    @Override
    public GlucoseReading execute(Command command) {
        loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        GlucoseReading reading = GlucoseReading.create(
                command.patientId(),
                command.value(),
                command.unit(),
                command.readingType(),
                command.measuredAt(),
                command.notes(),
                command.deviceSource()
        );
        return saveGlucoseReadingPort.save(reading);
    }
}