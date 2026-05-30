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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final DeleteGlucoseReadingUseCase deleteGlucoseReadingUseCase;
    private final GlucoseReadingPresentationMapper readingMapper;
    private final GlucoseStatsPresentationMapper statsMapper;

    @PostMapping("/{patientId}")
    public ResponseEntity<GlucoseReadingResponse> register(
            @PathVariable UUID patientId,
            @Valid @RequestBody RegisterGlucoseRequest request) {

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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return ResponseEntity.ok(statsMapper.toResponse(
                getGlucoseStatsUseCase.getStats(patientId, from, to)));
    }

    @DeleteMapping("/{patientId}/{readingId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID patientId,
            @PathVariable UUID readingId) {
        deleteGlucoseReadingUseCase.execute(readingId, patientId);
        return ResponseEntity.noContent().build();
    }
}