package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.CreateGlucoseReminderUseCase;
import com.diabecare.application.port.in.DeleteGlucoseReminderUseCase;
import com.diabecare.application.port.in.GetGlucoseRemindersUseCase;
import com.diabecare.application.port.in.ToggleGlucoseReminderUseCase;
import com.diabecare.domain.model.GlucoseReminder;
import com.diabecare.presentation.dto.request.CreateGlucoseReminderRequest;
import com.diabecare.presentation.dto.request.ToggleGlucoseReminderRequest;
import com.diabecare.presentation.dto.response.GlucoseReminderResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/glucose-reminders")
@RequiredArgsConstructor
@Tag(name = "Recordatorios de glucosa")
public class GlucoseReminderController {

    private final CreateGlucoseReminderUseCase createGlucoseReminderUseCase;
    private final GetGlucoseRemindersUseCase getGlucoseRemindersUseCase;
    private final ToggleGlucoseReminderUseCase toggleGlucoseReminderUseCase;
    private final DeleteGlucoseReminderUseCase deleteGlucoseReminderUseCase;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/{patientId}")
    public ResponseEntity<List<GlucoseReminderResponse>> getAll(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(getGlucoseRemindersUseCase.execute(patientId).stream()
                .map(this::toResponse).toList());
    }

    @PostMapping("/{patientId}")
    public ResponseEntity<GlucoseReminderResponse> create(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreateGlucoseReminderRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        GlucoseReminder reminder = createGlucoseReminderUseCase.execute(
                new CreateGlucoseReminderUseCase.Command(patientId, request.reminderTime(), request.label()));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reminder));
    }

    @PatchMapping("/{patientId}/{reminderId}")
    public ResponseEntity<GlucoseReminderResponse> toggle(
            @PathVariable UUID patientId,
            @PathVariable UUID reminderId,
            @Valid @RequestBody ToggleGlucoseReminderRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        GlucoseReminder reminder = toggleGlucoseReminderUseCase.execute(patientId, reminderId, request.enabled());

        return ResponseEntity.ok(toResponse(reminder));
    }

    @DeleteMapping("/{patientId}/{reminderId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID patientId,
            @PathVariable UUID reminderId,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);
        deleteGlucoseReminderUseCase.execute(patientId, reminderId);

        return ResponseEntity.noContent().build();
    }

    private GlucoseReminderResponse toResponse(GlucoseReminder reminder) {
        return new GlucoseReminderResponse(
                reminder.getId(), reminder.getReminderTime(), reminder.getLabel(), reminder.isEnabled());
    }
}
