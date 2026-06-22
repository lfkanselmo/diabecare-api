package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.UpdateInsulinProfileUseCase;
import com.diabecare.application.port.in.UpdatePatientUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.Patient;
import com.diabecare.presentation.dto.request.UpdateInsulinProfileRequest;
import com.diabecare.presentation.dto.request.UpdatePatientRequest;
import com.diabecare.presentation.dto.response.PatientResponse;
import com.diabecare.presentation.mapper.PatientPresentationMapper;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final LoadPatientPort loadPatientPort;
    private final UpdatePatientUseCase updatePatientUseCase;
    private final UpdateInsulinProfileUseCase updateInsulinProfileUseCase;
    private final PatientPresentationMapper mapper;
    private final CurrentUserResolver currentUserResolver;

    @PatchMapping("/{patientId}/insulin-profile")
    public ResponseEntity<PatientResponse> updateInsulinProfile(
            @PathVariable UUID patientId,
            @Valid @RequestBody UpdateInsulinProfileRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        Patient patient = updateInsulinProfileUseCase.execute(
                new UpdateInsulinProfileUseCase.Command(
                        patientId,
                        request.sensitivityFactor(),
                        request.carbRatio(),
                        request.targetGlucose()
                ));

        return ResponseEntity.ok(mapper.toResponse(patient));
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientResponse> update(
            @PathVariable UUID patientId,
            @Valid @RequestBody UpdatePatientRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        Patient patient = updatePatientUseCase.execute(
                new UpdatePatientUseCase.Command(
                        patientId,
                        request.heightCm(),
                        request.targetGlucoseMin(),
                        request.targetGlucoseMax(),
                        request.dailyCalorieGoal(),
                        request.activityLevel() != null
                                ? ActivityLevel.valueOf(request.activityLevel()) : null,
                        request.preferredGlucoseUnit() != null
                                ? GlucoseUnit.valueOf(request.preferredGlucoseUnit()) : null
                ));

        return ResponseEntity.ok(mapper.toResponse(patient));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getById(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return loadPatientPort.findById(patientId)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}