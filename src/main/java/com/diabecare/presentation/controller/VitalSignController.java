package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetVitalSignsUseCase;
import com.diabecare.application.port.in.RegisterVitalSignUseCase;
import com.diabecare.presentation.dto.request.RegisterVitalSignRequest;
import com.diabecare.presentation.dto.response.VitalSignResponse;
import com.diabecare.presentation.mapper.VitalSignPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vitals")
@RequiredArgsConstructor
public class VitalSignController {

    private final RegisterVitalSignUseCase registerVitalSignUseCase;
    private final GetVitalSignsUseCase getVitalSignsUseCase;
    private final VitalSignPresentationMapper mapper;

    @PostMapping("/{patientId}")
    public ResponseEntity<VitalSignResponse> register(
            @PathVariable UUID patientId,
            @RequestBody RegisterVitalSignRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                mapper.toResponse(registerVitalSignUseCase.execute(
                        new RegisterVitalSignUseCase.Command(
                                patientId,
                                request.weightKg(),
                                request.heightCm(),
                                request.systolicBp(),
                                request.diastolicBp(),
                                request.heartRate(),
                                request.hba1c(),
                                request.measuredAt(),
                                request.notes()
                        ))));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<List<VitalSignResponse>> getAll(@PathVariable UUID patientId) {
        return ResponseEntity.ok(getVitalSignsUseCase.getByPatientId(patientId)
                .stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{patientId}/latest")
    public ResponseEntity<VitalSignResponse> getLatest(@PathVariable UUID patientId) {
        return getVitalSignsUseCase.getLatest(patientId)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}