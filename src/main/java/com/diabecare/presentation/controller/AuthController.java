package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.LoginUseCase;
import com.diabecare.application.port.in.RegisterUseCase;
import com.diabecare.application.port.in.GetPatientUseCase;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.presentation.dto.request.LoginRequest;
import com.diabecare.presentation.dto.request.RegisterRequest;
import com.diabecare.presentation.dto.response.AuthResponse;
import com.diabecare.presentation.mapper.PatientPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación")
public class AuthController {

    private final LoginUseCase           loginUseCase;
    private final RegisterUseCase        registerUseCase;
    private final GetPatientUseCase      getPatientUseCase;
    private final PatientPresentationMapper patientMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario y paciente")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUseCase.Result result = registerUseCase.execute(
                new RegisterUseCase.Command(
                        request.email(),
                        request.password(),
                        request.fullName(),
                        LocalDate.parse(request.dateOfBirth()),
                        DiabetesType.valueOf(request.diabetesType()),
                        LocalDate.parse(request.diagnosisDate()),
                        request.heightCm() != null ? new BigDecimal(request.heightCm()) : null,
                        BiologicalSex.valueOf(request.biologicalSex())
                ));

        var patient = getPatientUseCase.getByUserId(UUID.fromString(result.userId()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(
                        result.token(),
                        result.expiresIn(),
                        patientMapper.toResponse(patient.patient())
                ));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUseCase.Result result = loginUseCase.execute(
                new LoginUseCase.Command(request.email(), request.password()));

        var patient = getPatientUseCase.getByUserId(UUID.fromString(result.userId()));

        return ResponseEntity.ok(AuthResponse.of(
                result.token(),
                result.expiresIn(),
                patientMapper.toResponse(patient.patient())
        ));
    }
}