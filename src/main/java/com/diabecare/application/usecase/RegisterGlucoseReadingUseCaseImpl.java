package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterGlucoseReadingUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveGlucoseReadingPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterGlucoseReadingUseCaseImpl implements RegisterGlucoseReadingUseCase {

    private final SaveGlucoseReadingPort saveGlucoseReadingPort;
    private final LoadPatientPort        loadPatientPort;
    private final RateLimitService       rateLimitService;

    @Override
    public GlucoseReading execute(Command command) {
        rateLimitService.checkGlucoseLimit(command.patientId());

        loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        GlucoseReading reading = command.clientReadingId() != null
                ? GlucoseReading.createWithId(
                        command.clientReadingId(),
                        command.patientId(),
                        command.value(),
                        command.unit(),
                        command.readingType(),
                        command.measuredAt(),
                        command.notes(),
                        command.deviceSource())
                : GlucoseReading.create(
                        command.patientId(),
                        command.value(),
                        command.unit(),
                        command.readingType(),
                        command.measuredAt(),
                        command.notes(),
                        command.deviceSource());
        return saveGlucoseReadingPort.save(reading);
    }
}