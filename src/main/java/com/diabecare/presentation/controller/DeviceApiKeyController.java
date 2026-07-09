package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GenerateDeviceApiKeyUseCase;
import com.diabecare.application.port.in.ListDeviceApiKeysUseCase;
import com.diabecare.application.port.in.RevokeDeviceApiKeyUseCase;
import com.diabecare.presentation.dto.request.GenerateDeviceApiKeyRequest;
import com.diabecare.presentation.dto.response.DeviceApiKeyResponse;
import com.diabecare.presentation.dto.response.GeneratedDeviceApiKeyResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Gestión (JWT-autenticada) de las API keys que un paciente puede emitir para que un
 * bridge externo (CGM, Nightscout, etc.) importe lecturas sin login interactivo — ver
 * {@link DeviceGlucoseImportController} para el endpoint que consumen esas keys.
 */
@RestController
@RequestMapping("/api/v1/device-keys")
@RequiredArgsConstructor
public class DeviceApiKeyController {

    private final GenerateDeviceApiKeyUseCase generateDeviceApiKeyUseCase;
    private final ListDeviceApiKeysUseCase listDeviceApiKeysUseCase;
    private final RevokeDeviceApiKeyUseCase revokeDeviceApiKeyUseCase;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{patientId}")
    public ResponseEntity<GeneratedDeviceApiKeyResponse> generate(
            @PathVariable UUID patientId,
            @Valid @RequestBody GenerateDeviceApiKeyRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        GenerateDeviceApiKeyUseCase.Result result = generateDeviceApiKeyUseCase.execute(
                new GenerateDeviceApiKeyUseCase.Command(patientId, request.label()));

        return ResponseEntity.status(HttpStatus.CREATED).body(new GeneratedDeviceApiKeyResponse(
                result.id(), result.rawKey(), result.label(), result.createdAt()));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<List<DeviceApiKeyResponse>> list(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        List<DeviceApiKeyResponse> keys = listDeviceApiKeysUseCase.execute(patientId).stream()
                .map(k -> new DeviceApiKeyResponse(
                        k.getId(), k.getLabel(), k.getCreatedAt(), k.getLastUsedAt(), k.isRevoked()))
                .toList();

        return ResponseEntity.ok(keys);
    }

    @DeleteMapping("/{patientId}/{keyId}")
    public ResponseEntity<Void> revoke(
            @PathVariable UUID patientId, @PathVariable UUID keyId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        revokeDeviceApiKeyUseCase.execute(new RevokeDeviceApiKeyUseCase.Command(patientId, keyId));
        return ResponseEntity.noContent().build();
    }
}
