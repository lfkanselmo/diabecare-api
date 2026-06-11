package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetPatientUseCase;
import com.diabecare.application.port.in.RegisterPatientUseCase;
import com.diabecare.application.port.in.RegisterUserUseCase;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.infrastructure.config.JwtProperties;
import com.diabecare.infrastructure.security.jwt.JwtService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación")
public class AuthController {

    private final RegisterUserUseCase       registerUserUseCase;
    private final RegisterPatientUseCase    registerPatientUseCase;
    private final GetPatientUseCase         getPatientUseCase;
    private final AuthenticationManager     authenticationManager;
    private final UserDetailsService        userDetailsService;
    private final LoadUserPort              loadUserPort;
    private final JwtService                jwtService;
    private final JwtProperties             jwtProperties;
    private final PatientPresentationMapper patientMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario y paciente")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        var userResult = registerUserUseCase.execute(
                new RegisterUserUseCase.Command(request.email(), request.password()));

        var patientResult = registerPatientUseCase.execute(
                new RegisterPatientUseCase.Command(
                        userResult.id(),
                        request.fullName(),
                        LocalDate.parse(request.dateOfBirth()),
                        DiabetesType.valueOf(request.diabetesType()),
                        LocalDate.parse(request.diagnosisDate()),
                        request.heightCm() != null ? new BigDecimal(request.heightCm()) : null,
                        BiologicalSex.valueOf(request.biologicalSex())
                ));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateAccessToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(token,
                        jwtProperties.getAccessTokenExpiryMs(),
                        patientMapper.toResponse(patientResult)));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateAccessToken(userDetails);

        var userId = loadUserPort.findUserIdByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var patientResult = getPatientUseCase.getByUserId(userId);

        return ResponseEntity.ok(AuthResponse.of(token,
                jwtProperties.getAccessTokenExpiryMs(),
                patientMapper.toResponse(patientResult.patient())));
    }
}