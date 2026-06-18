package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetSystemConfigUseCase;
import com.diabecare.domain.model.SystemConfig;
import com.diabecare.presentation.dto.response.SystemConfigResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system-config")
@RequiredArgsConstructor
@Tag(name = "Configuración del sistema")
public class SystemConfigController {

    private final GetSystemConfigUseCase getSystemConfigUseCase;

    @GetMapping
    public ResponseEntity<List<SystemConfigResponse>> getAll() {
        return ResponseEntity.ok(
                getSystemConfigUseCase.getAll().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @PostMapping("/reload")
    public ResponseEntity<Void> reload() {
        getSystemConfigUseCase.reload();
        return ResponseEntity.ok().build();
    }

    private SystemConfigResponse toResponse(SystemConfig config) {
        return new SystemConfigResponse(
                config.getKey(),
                config.getValue(),
                config.getDataType().name(),
                config.getCategory().name(),
                config.getDescription(),
                config.getUpdatedAt()
        );
    }
}