package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.*;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.presentation.dto.request.RegisterGlucoseRequest;
import com.diabecare.presentation.dto.response.GlucoseCorrelationResponse;
import com.diabecare.presentation.dto.response.GlucoseReadingResponse;
import com.diabecare.presentation.dto.response.GlucoseStatsResponse;
import com.diabecare.presentation.mapper.GlucoseReadingPresentationMapper;
import com.diabecare.presentation.mapper.GlucoseStatsPresentationMapper;
import com.diabecare.presentation.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/glucose")
@RequiredArgsConstructor
public class GlucoseController {

    private final RegisterGlucoseReadingUseCase registerGlucoseReadingUseCase;
    private final GetGlucoseHistoryUseCase getGlucoseHistoryUseCase;
    private final GetGlucoseStatsUseCase getGlucoseStatsUseCase;
    private final GetLatestGlucoseReadingUseCase getLatestGlucoseReadingUseCase;
    private final DeleteGlucoseReadingUseCase deleteGlucoseReadingUseCase;
    private final GlucoseReadingPresentationMapper readingMapper;
    private final GlucoseStatsPresentationMapper statsMapper;
    private final ExportGlucoseDataUseCase exportGlucoseDataUseCase;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{patientId}")
    public ResponseEntity<GlucoseReadingResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody RegisterGlucoseRequest request,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        GlucoseReading reading = registerGlucoseReadingUseCase.execute(
                new RegisterGlucoseReadingUseCase.Command(
                        patientId,
                        request.value(),
                        GlucoseUnit.valueOf(request.unit()),
                        ReadingType.valueOf(request.readingType()),
                        request.measuredAt(),
                        request.notes(),
                        request.deviceSource()
                ));
        return ResponseEntity.status(HttpStatus.CREATED).body(readingMapper.toResponse(reading));
    }

    @GetMapping("/{patientId}/history")
    public ResponseEntity<GlucoseCorrelationResponse> getHistory(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        GetGlucoseHistoryUseCase.Result result =
                getGlucoseHistoryUseCase.getByPatientAndDateRange(patientId, from, to);

        List<GlucoseReadingResponse> readings = result.readings()
                .stream().map(readingMapper::toResponse).toList();

        List<GlucoseCorrelationResponse.MealMarkerResponse> markers = result.mealEntries()
                .stream()
                .map(m -> new GlucoseCorrelationResponse.MealMarkerResponse(
                        m.getConsumedAt().toString(),
                        m.getMealType().name(),
                        m.getTotalCalories().doubleValue(),
                        m.getTotalCarbohydrates().doubleValue()
                ))
                .toList();

        return ResponseEntity.ok(new GlucoseCorrelationResponse(readings, markers));
    }

    @GetMapping("/{patientId}/stats")
    public ResponseEntity<GlucoseStatsResponse> getStats(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return ResponseEntity.ok(statsMapper.toResponse(
                getGlucoseStatsUseCase.getStats(patientId, from, to)));
    }

    @GetMapping("/{patientId}/latest")
    public ResponseEntity<GlucoseReadingResponse> getLatest(
            @PathVariable UUID patientId,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        return getLatestGlucoseReadingUseCase.getLatest(patientId)
                .map(reading -> ResponseEntity.ok(readingMapper.toResponse(reading)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{patientId}/{readingId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID patientId,
            @PathVariable UUID readingId,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);
        deleteGlucoseReadingUseCase.execute(readingId, patientId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{patientId}/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        String csv = exportGlucoseDataUseCase.exportAsCsv(patientId, from, to);
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"glucosa.csv\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(bytes);
    }

    @GetMapping("/{patientId}/export/json")
    public ResponseEntity<byte[]> exportJson(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication) {

        currentUserResolver.verifyOwnsPatient(patientId, authentication);

        String json = exportGlucoseDataUseCase.exportAsJson(patientId, from, to);
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"glucosa.json\"")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(bytes);
    }
}