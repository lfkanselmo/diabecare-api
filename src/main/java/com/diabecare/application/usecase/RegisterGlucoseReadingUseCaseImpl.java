package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterGlucoseReadingUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveGlucoseReadingPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.service.RateLimitService;
import com.diabecare.infrastructure.config.RateLimitConfig;
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
        rateLimitService.checkLimit(
                command.patientId(),
                "GLUCOSE",
                RateLimitConfig::createGlucoseBucket
        );

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