package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetAlertsUseCase;
import com.diabecare.presentation.dto.response.AlertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final GetAlertsUseCase getAlertsUseCase;

    @GetMapping("/{patientId}")
    public ResponseEntity<List<AlertResponse>> getAlerts(@PathVariable UUID patientId) {
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