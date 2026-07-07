package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetAlertsUseCase;
import com.diabecare.presentation.dto.response.AlertResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final GetAlertsUseCase getAlertsUseCase;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/{patientId}")
    public ResponseEntity<List<AlertResponse>> getAlerts(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyCanReadPatient(patientId, authentication);

        return ResponseEntity.ok(
                getAlertsUseCase.getAlerts(patientId).stream()
                        .map(a -> new AlertResponse(
                                a.getType().name(),
                                a.getSeverity().name(),
                                a.getTitle(),
                                a.getMessage()))
                        .toList()
        );
    }
}