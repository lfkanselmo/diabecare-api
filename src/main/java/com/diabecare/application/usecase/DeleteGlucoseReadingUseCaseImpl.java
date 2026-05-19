package com.diabecare.application.usecase;

import com.diabecare.application.port.in.DeleteGlucoseReadingUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.exception.GlucoseReadingNotFoundException;
import com.diabecare.domain.exception.InvalidGlucoseReadingException;
import com.diabecare.domain.model.GlucoseReading;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteGlucoseReadingUseCaseImpl implements DeleteGlucoseReadingUseCase {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;

    @Override
    public void execute(UUID readingId, UUID patientId) {
        GlucoseReading reading = loadGlucoseReadingPort.findById(readingId)
                .orElseThrow(() -> new GlucoseReadingNotFoundException(readingId.toString()));

        if (!reading.getPatientId().equals(patientId)) {
            throw new InvalidGlucoseReadingException(
                    "No tienes permisos para eliminar esta lectura");
        }
        loadGlucoseReadingPort.deleteById(readingId);
    }
}