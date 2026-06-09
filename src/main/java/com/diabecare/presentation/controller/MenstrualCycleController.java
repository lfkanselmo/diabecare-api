package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetMenstrualCycleStatusUseCase;
import com.diabecare.application.port.in.RegisterMenstrualCycleUseCase;
import com.diabecare.domain.model.CyclePhase;
import com.diabecare.presentation.dto.request.MenstrualCycleRequest;
import com.diabecare.presentation.dto.response.MenstrualCycleStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menstrual-cycle")
@RequiredArgsConstructor
public class MenstrualCycleController {

    private final RegisterMenstrualCycleUseCase registerUseCase;
    private final GetMenstrualCycleStatusUseCase getStatusUseCase;

    private static final Map<CyclePhase, String> PHASE_LABELS = Map.of(
            CyclePhase.MENSTRUATION,  "Menstruación",
            CyclePhase.FOLLICULAR,    "Fase folicular",
            CyclePhase.OVULATION,     "Ovulación",
            CyclePhase.LUTEAL_EARLY,  "Fase lútea temprana",
            CyclePhase.LUTEAL_LATE,   "Fase lútea tardía"
    );

    @PostMapping("/{patientId}")
    public ResponseEntity<MenstrualCycleStatusResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody MenstrualCycleRequest request) {

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
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(buildStatus(patientId));
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
                PHASE_LABELS.get(status.currentPhase()),
                status.dayOfCycle(),
                status.nextCycleStart(),
                (int) daysUntilNext,
                status.glucoseGuidance(),
                status.averageCycleLength(),
                history
        );
    }
}