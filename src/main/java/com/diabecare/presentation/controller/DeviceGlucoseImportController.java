package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.ImportGlucoseReadingsUseCase;
import com.diabecare.domain.exception.InvalidDeviceApiKeyException;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.presentation.dto.request.ImportGlucoseReadingsRequest;
import com.diabecare.presentation.dto.response.GlucoseReadingResponse;
import com.diabecare.presentation.mapper.GlucoseReadingPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint público a nivel de Spring Security (ver {@code PublicEndpoints}) — no usa
 * JWT porque un bridge externo desatendido (CGM, Nightscout) no puede hacer login
 * interactivo. Se autentica con una API key de dispositivo en un header propio,
 * validada manualmente dentro del caso de uso (mismo espíritu que /auth/**, que
 * también es público a nivel de filtro y valida credenciales por su cuenta).
 */
@RestController
@RequestMapping("/api/v1/glucose/import")
@RequiredArgsConstructor
public class DeviceGlucoseImportController {

    private static final String API_KEY_HEADER = "X-Device-Api-Key";

    private final ImportGlucoseReadingsUseCase importGlucoseReadingsUseCase;
    private final GlucoseReadingPresentationMapper readingMapper;

    @PostMapping
    public ResponseEntity<List<GlucoseReadingResponse>> importReadings(
            @RequestHeader(API_KEY_HEADER) String apiKey,
            @Valid @RequestBody ImportGlucoseReadingsRequest request) {

        if (apiKey.isBlank()) {
            throw new InvalidDeviceApiKeyException("API key de dispositivo inválida.");
        }

        List<ImportGlucoseReadingsUseCase.ReadingInput> inputs = request.readings().stream()
                .map(entry -> new ImportGlucoseReadingsUseCase.ReadingInput(
                        entry.value(),
                        GlucoseUnit.valueOf(entry.unit()),
                        ReadingType.valueOf(entry.readingType()),
                        entry.measuredAt()))
                .toList();

        List<GlucoseReading> imported = importGlucoseReadingsUseCase.execute(
                new ImportGlucoseReadingsUseCase.Command(apiKey, inputs));

        List<GlucoseReadingResponse> responses = imported.stream().map(readingMapper::toResponse).toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
}
