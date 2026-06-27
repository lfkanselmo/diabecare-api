package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.FinishPeriodUseCase;
import com.diabecare.application.port.in.GetCyclePhaseCalendarUseCase;
import com.diabecare.application.port.in.GetMenstrualCycleStatusUseCase;
import com.diabecare.application.port.in.RegisterCycleDayEntryUseCase;
import com.diabecare.application.port.in.RegisterMenstrualCycleUseCase;
import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CycleSymptom;
import com.diabecare.domain.model.FlowIntensity;
import com.diabecare.domain.model.SymptomSeverity;
import com.diabecare.domain.service.CycleLabelService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.diabecare.presentation.dto.request.FinishPeriodRequest;
import com.diabecare.presentation.dto.request.MenstrualCycleRequest;
import com.diabecare.presentation.dto.request.RegisterCycleDayEntryRequest;
import com.diabecare.presentation.dto.response.CycleDayEntryResponse;
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
    private final RegisterCycleDayEntryUseCase registerDayEntryUseCase;
    private final FinishPeriodUseCase finishPeriodUseCase;
    private final GetMenstrualCycleStatusUseCase getStatusUseCase;
    private final GetCyclePhaseCalendarUseCase getCyclePhaseCalendarUseCase;
    private final CurrentUserResolver currentUserResolver;
    private final MenstrualCycleGuidanceService cycleGuidanceService;
    private final CycleLabelService cycleLabelService;

    @PostMapping("/{patientId}")
    public ResponseEntity<MenstrualCycleStatusResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody MenstrualCycleRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        registerUseCase.execute(new RegisterMenstrualCycleUseCase.Command(
                patientId,
                request.startDate(),
                request.notes()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildStatus(patientId));
    }

    @PostMapping("/{patientId}/finish-period")
    public ResponseEntity<MenstrualCycleStatusResponse> finishPeriod(
            @PathVariable UUID patientId,
            @Valid @RequestBody FinishPeriodRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        finishPeriodUseCase.execute(new FinishPeriodUseCase.Command(patientId, request.endDate()));

        return ResponseEntity.ok(buildStatus(patientId));
    }

    @PostMapping("/{patientId}/days")
    public ResponseEntity<CycleDayEntryResponse> registerDayEntry(
            @PathVariable UUID patientId,
            @Valid @RequestBody RegisterCycleDayEntryRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        var symptoms = request.symptoms() == null ? List.<RegisterCycleDayEntryUseCase.SymptomInput>of() :
                request.symptoms().stream()
                        .map(s -> new RegisterCycleDayEntryUseCase.SymptomInput(
                                CycleSymptom.valueOf(s.symptom()),
                                SymptomSeverity.valueOf(s.severity())))
                        .toList();

        CycleDayEntry entry = registerDayEntryUseCase.execute(new RegisterCycleDayEntryUseCase.Command(
                patientId,
                request.entryDate(),
                FlowIntensity.valueOf(request.flowIntensity()),
                request.notes(),
                symptoms
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entry));
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
                                c.getStartDate(),
                                c.getEndDate(),
                                c.getActualPeriodLengthDays()))
                        .toList();

        CycleDayEntryResponse todayEntry = status.todayEntry() != null
                ? toResponse(status.todayEntry())
                : null;

        return new MenstrualCycleStatusResponse(
                status.currentPhase().name(),
                cycleGuidanceService.resolveLabel(status.currentPhase()),
                status.dayOfCycle(),
                status.isOngoing(),
                status.isOpenTooLong(),
                status.isProjectionStale(),
                status.periodStartDate(),
                status.nextCycleStart(),
                (int) daysUntilNext,
                status.glucoseGuidance(),
                status.averageCycleLength(),
                status.averagePeriodLength(),
                todayEntry,
                history
        );
    }

    private CycleDayEntryResponse toResponse(CycleDayEntry entry) {
        List<CycleDayEntryResponse.SymptomResponse> symptoms = entry.getSymptoms().stream()
                .map(s -> new CycleDayEntryResponse.SymptomResponse(
                        s.getSymptom().name(),
                        cycleLabelService.resolveSymptomLabel(s.getSymptom()),
                        s.getSeverity().name()))
                .toList();

        return new CycleDayEntryResponse(
                entry.getDayEntryId().toString(),
                entry.getEntryDate(),
                entry.getFlowIntensity().name(),
                cycleLabelService.resolveFlowLabel(entry.getFlowIntensity()),
                entry.getNotes(),
                symptoms
        );
    }
}