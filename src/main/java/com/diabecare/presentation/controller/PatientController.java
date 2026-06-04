package com.diabecare.presentation.controller;

import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.presentation.dto.request.UpdateInsulinProfileRequest;
import com.diabecare.presentation.dto.response.PatientResponse;
import com.diabecare.presentation.mapper.PatientPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final LoadPatientPort loadPatientPort;
    private final SavePatientPort savePatientPort;
    private final PatientPresentationMapper mapper;

    @PatchMapping("/{patientId}/insulin-profile")
    public ResponseEntity<PatientResponse> updateInsulinProfile(
            @PathVariable UUID patientId,
            @Valid @RequestBody UpdateInsulinProfileRequest request) {

        return loadPatientPort.findById(patientId)
                .map(patient -> {
                    patient.updateInsulinProfile(
                            request.sensitivityFactor(),
                            request.carbRatio(),
                            request.targetGlucose());
                    return ResponseEntity.ok(mapper.toResponse(savePatientPort.save(patient)));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}