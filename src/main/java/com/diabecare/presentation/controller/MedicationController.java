package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.DeactivateMedicationUseCase;
import com.diabecare.application.port.in.GetMedicationsUseCase;
import com.diabecare.application.port.in.RegisterMedicationUseCase;
import com.diabecare.application.port.in.SyncMedicationsUseCase;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.presentation.dto.request.RegisterMedicationRequest;
import com.diabecare.presentation.dto.response.MedicationResponse;
import com.diabecare.presentation.mapper.MedicationPresentationMapper;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final RegisterMedicationUseCase registerMedicationUseCase;
    private final GetMedicationsUseCase getMedicationsUseCase;
    private final DeactivateMedicationUseCase deactivateMedicationUseCase;
    private final SyncMedicationsUseCase syncMedicationsUseCase;
    private final MedicationPresentationMapper mapper;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{patientId}")
    public ResponseEntity<MedicationResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody RegisterMedicationRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                mapper.toResponse(registerMedicationUseCase.execute(
                        new RegisterMedicationUseCase.Command(
                                patientId,
                                request.name(),
                                MedicationType.valueOf(request.type()),
                                request.dose(),
                                DoseUnit.valueOf(request.doseUnit()),
                                MedicationFrequency.valueOf(request.frequency()),
                                request.startDate(),
                                request.notes(),
                                request.medicationId()
                        ))));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<List<MedicationResponse>> getActive(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(getMedicationsUseCase.getActiveByPatientId(patientId)
                .stream().map(mapper::toResponse).toList());
    }

    /**
     * Cursor de sincronización incremental para el motor offline-first del móvil —
     * a diferencia de {@code GET /{patientId}} (solo activos), acá se incluyen
     * también los desactivados. {@code since} ausente trae el historial completo
     * (primera sincronización).
     */
    @GetMapping("/{patientId}/sync")
    public ResponseEntity<List<MedicationResponse>> sync(
            @PathVariable UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        List<MedicationResponse> medications = syncMedicationsUseCase.execute(patientId, since).stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(medications);
    }

    @DeleteMapping("/{patientId}/{medicationId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID patientId,
            @PathVariable UUID medicationId,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);
        deactivateMedicationUseCase.execute(medicationId, patientId);
        return ResponseEntity.noContent().build();
    }
}