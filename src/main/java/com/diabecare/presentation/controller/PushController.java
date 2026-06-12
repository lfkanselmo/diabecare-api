package com.diabecare.presentation.controller;

import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.infrastructure.push.PushNotificationService;
import com.diabecare.infrastructure.config.DiabeCareProperties;
import com.diabecare.presentation.dto.request.PushSubscriptionRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
@Tag(name = "Notificaciones Push")
public class PushController {

    private final PushNotificationService pushService;
    private final LoadUserPort            loadUserPort;
    private final LoadPatientPort         loadPatientPort;
    private final DiabeCareProperties     properties;

    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", properties.push().vapidPublicKey()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PushSubscriptionRequest request) {

        loadUserPort.findUserIdByEmail(userDetails.getUsername())
                .flatMap(loadPatientPort::findByUserId)
                .ifPresent(patient -> pushService.subscribe(
                        patient.getPatientId(),
                        request.endpoint(),
                        request.p256dh(),
                        request.auth()
                ));

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody Map<String, String> body) {
        pushService.unsubscribe(body.get("endpoint"));
        return ResponseEntity.ok().build();
    }
}