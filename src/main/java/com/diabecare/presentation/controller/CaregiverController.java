package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.*;
import com.diabecare.presentation.dto.request.RedeemCaregiverInviteRequest;
import com.diabecare.presentation.dto.response.CaregiverInviteResponse;
import com.diabecare.presentation.dto.response.CaregiverLinkResponse;
import com.diabecare.presentation.dto.response.PatientAccessResponse;
import com.diabecare.presentation.dto.response.RedeemCaregiverInviteResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/caregivers")
@RequiredArgsConstructor
public class CaregiverController {

    private final CreateCaregiverInviteUseCase createCaregiverInviteUseCase;
    private final RedeemCaregiverInviteUseCase redeemCaregiverInviteUseCase;
    private final ListMyCaregiversUseCase listMyCaregiversUseCase;
    private final ListPatientsICareForUseCase listPatientsICareForUseCase;
    private final RevokeCaregiverAccessUseCase revokeCaregiverAccessUseCase;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{patientId}/invites")
    public ResponseEntity<CaregiverInviteResponse> createInvite(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        CreateCaregiverInviteUseCase.Result result = createCaregiverInviteUseCase.execute(patientId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CaregiverInviteResponse(result.code(), result.expiresAt()));
    }

    @GetMapping("/{patientId}/links")
    public ResponseEntity<List<CaregiverLinkResponse>> getLinks(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        List<CaregiverLinkResponse> links = listMyCaregiversUseCase.execute(patientId).stream()
                .map(v -> new CaregiverLinkResponse(
                        v.linkId(), v.caregiverUserId(), v.caregiverName(), v.caregiverEmail(), v.linkedAt()))
                .toList();
        return ResponseEntity.ok(links);
    }

    @DeleteMapping("/{patientId}/links/{linkId}")
    public ResponseEntity<Void> revokeLink(
            @PathVariable UUID patientId, @PathVariable UUID linkId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        revokeCaregiverAccessUseCase.execute(new RevokeCaregiverAccessUseCase.Command(patientId, linkId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/redeem")
    public ResponseEntity<RedeemCaregiverInviteResponse> redeem(
            @Valid @RequestBody RedeemCaregiverInviteRequest request, Authentication authentication) {

        UUID caregiverUserId = currentUserResolver.resolveUserId(authentication);

        RedeemCaregiverInviteUseCase.Result result = redeemCaregiverInviteUseCase.execute(
                new RedeemCaregiverInviteUseCase.Command(request.code(), caregiverUserId));

        return ResponseEntity.ok(
                new RedeemCaregiverInviteResponse(result.patientId(), result.patientFullName()));
    }

    @GetMapping("/my-patients")
    public ResponseEntity<List<PatientAccessResponse>> getMyPatients(Authentication authentication) {

        UUID caregiverUserId = currentUserResolver.resolveUserId(authentication);

        List<PatientAccessResponse> patients = listPatientsICareForUseCase.execute(caregiverUserId).stream()
                .map(v -> new PatientAccessResponse(v.patientId(), v.patientFullName(), v.linkedAt()))
                .toList();
        return ResponseEntity.ok(patients);
    }
}
