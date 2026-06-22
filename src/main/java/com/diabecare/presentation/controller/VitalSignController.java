package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetHba1cTrendUseCase;
import com.diabecare.application.port.in.GetVitalSignsUseCase;
import com.diabecare.application.port.in.RegisterVitalSignUseCase;
import com.diabecare.presentation.dto.request.RegisterVitalSignRequest;
import com.diabecare.presentation.dto.response.Hba1cTrendResponse;
import com.diabecare.presentation.dto.response.VitalSignResponse;
import com.diabecare.presentation.mapper.VitalSignPresentationMapper;
import com.diabecare.presentation.util.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final GetHba1cTrendUseCase getHba1cTrendUseCase;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{patientId}")
    public ResponseEntity<VitalSignResponse> register(
            @PathVariable UUID patientId,
            @RequestBody RegisterVitalSignRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

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
    public ResponseEntity<List<VitalSignResponse>> getAll(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(getVitalSignsUseCase.getByPatientId(patientId)
                .stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{patientId}/latest")
    public ResponseEntity<VitalSignResponse> getLatest(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return getVitalSignsUseCase.getLatest(patientId)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{patientId}/hba1c-trend")
    public ResponseEntity<List<Hba1cTrendResponse>> getHba1cTrend(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "6") int months,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(
                getHba1cTrendUseCase.getTrend(patientId, months)
                        .stream()
                        .map(m -> new Hba1cTrendResponse(
                                m.month(),
                                m.estimatedHba1c(),
                                m.averageGlucose(),
                                m.totalReadings()))
                        .toList()
        );
    }
}