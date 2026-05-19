package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.DeactivateMedicationUseCase;
import com.diabecare.application.port.in.GetMedicationsUseCase;
import com.diabecare.application.port.in.RegisterMedicationUseCase;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.presentation.dto.request.RegisterMedicationRequest;
import com.diabecare.presentation.dto.response.MedicationResponse;
import com.diabecare.presentation.mapper.MedicationPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final RegisterMedicationUseCase registerMedicationUseCase;
    private final GetMedicationsUseCase getMedicationsUseCase;
    private final DeactivateMedicationUseCase deactivateMedicationUseCase;
    private final MedicationPresentationMapper mapper;

    @PostMapping("/{patientId}")
    public ResponseEntity<MedicationResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody RegisterMedicationRequest request) {

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
                                request.notes()
                        ))));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<List<MedicationResponse>> getActive(@PathVariable UUID patientId) {
        return ResponseEntity.ok(getMedicationsUseCase.getActiveByPatientId(patientId)
                .stream().map(mapper::toResponse).toList());
    }

    @DeleteMapping("/{patientId}/{medicationId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID patientId,
            @PathVariable UUID medicationId) {
        deactivateMedicationUseCase.execute(medicationId, patientId);
        return ResponseEntity.noContent().build();
    }
}