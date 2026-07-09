package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ImportGlucoseReadingsUseCase;
import com.diabecare.application.port.out.DeviceApiKeyPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveGlucoseReadingPort;
import com.diabecare.domain.exception.InvalidDeviceApiKeyException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.DeviceApiKey;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ImportGlucoseReadingsUseCaseImpl implements ImportGlucoseReadingsUseCase {

    private final DeviceApiKeyPort deviceApiKeyPort;
    private final SaveGlucoseReadingPort saveGlucoseReadingPort;
    private final LoadPatientPort loadPatientPort;
    private final RateLimitService rateLimitService;

    @Override
    public List<GlucoseReading> execute(Command command) {
        DeviceApiKey apiKey = deviceApiKeyPort.findByRawKey(command.rawApiKey())
                .orElseThrow(() -> new InvalidDeviceApiKeyException("API key de dispositivo inválida."));

        if (apiKey.isRevoked()) {
            throw new InvalidDeviceApiKeyException("Esta API key fue revocada.");
        }

        loadPatientPort.findById(apiKey.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(apiKey.getPatientId().toString()));

        // Un cupo por lectura (no por llamada) — un batch grande consume el bucket
        // proporcionalmente a los datos reales, igual que el registro manual.
        List<GlucoseReading> imported = command.readings().stream()
                .map(input -> {
                    rateLimitService.checkDeviceImportLimit(apiKey.getId());
                    GlucoseReading reading = GlucoseReading.create(
                            apiKey.getPatientId(),
                            input.value(),
                            input.unit(),
                            input.readingType(),
                            input.measuredAt(),
                            null,
                            apiKey.getLabel()
                    );
                    return saveGlucoseReadingPort.save(reading);
                })
                .toList();

        deviceApiKeyPort.touchLastUsed(apiKey.getId());

        return imported;
    }
}
