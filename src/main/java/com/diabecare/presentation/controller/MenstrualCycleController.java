package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetCyclePhaseCalendarUseCase;
import com.diabecare.application.port.in.GetMenstrualCycleStatusUseCase;
import com.diabecare.application.port.in.RegisterMenstrualCycleUseCase;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.diabecare.presentation.dto.request.MenstrualCycleRequest;
import com.diabecare.presentation.dto.response.CyclePhaseDayResponse;
import com.diabecare.presentation.dto.response.MenstrualCycleStatusResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menstrual-cycle")
@RequiredArgsConstructor
public class MenstrualCycleController {

    private final RegisterMenstrualCycleUseCase registerUseCase;
    private final GetMenstrualCycleStatusUseCase getStatusUseCase;
    private final GetCyclePhaseCalendarUseCase getCyclePhaseCalendarUseCase;
    private final CurrentUserResolver currentUserResolver;
    private final MenstrualCycleGuidanceService cycleGuidanceService;

    @PostMapping("/{patientId}")
    public ResponseEntity<MenstrualCycleStatusResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody MenstrualCycleRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        registerUseCase.execute(new RegisterMenstrualCycleUseCase.Command(
                patientId,
                request.startDate(),
                request.periodLengthDays(),
                request.symptoms(),
                request.notes()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildStatus(patientId));
    }

    @GetMapping("/{patientId}/status")
    public ResponseEntity<MenstrualCycleStatusResponse> getStatus(
            @PathVariable UUID patientId, Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(buildStatus(patientId));
    }

    @GetMapping("/{patientId}/phase-calendar")
    public ResponseEntity<List<CyclePhaseDayResponse>> getPhaseCalendar(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        List<CyclePhaseDayResponse> calendar = getCyclePhaseCalendarUseCase
                .getCalendar(patientId, from, to).stream()
                .map(day -> new CyclePhaseDayResponse(day.date(), day.phase().name()))
                .toList();

        return ResponseEntity.ok(calendar);
    }

    private MenstrualCycleStatusResponse buildStatus(UUID patientId) {
        GetMenstrualCycleStatusUseCase.CycleStatus status =
                getStatusUseCase.getStatus(patientId);

        long daysUntilNext = ChronoUnit.DAYS.between(
                LocalDate.now(), status.nextCycleStart());

        List<MenstrualCycleStatusResponse.CycleHistoryItem> history =
                status.history().stream()
                        .limit(6)
                        .map(c -> new MenstrualCycleStatusResponse.CycleHistoryItem(
                                c.getCycleId().toString(),
                                c.getCycleStartDate(),
                                c.getCycleLengthDays(),
                                c.getPeriodLengthDays(),
                                c.getSymptoms()))
                        .toList();

        return new MenstrualCycleStatusResponse(
                status.currentPhase().name(),
                cycleGuidanceService.resolveLabel(status.currentPhase()),
                status.dayOfCycle(),
                status.nextCycleStart(),
                (int) daysUntilNext,
                status.glucoseGuidance(),
                status.averageCycleLength(),
                history
        );
    }
}