package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetPatientUseCase;
import com.diabecare.application.port.in.RegisterPatientUseCase;
import com.diabecare.application.port.in.RegisterUserUseCase;
import com.diabecare.application.dto.UserRecord;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import com.diabecare.infrastructure.config.JwtProperties;
import com.diabecare.infrastructure.persistence.repository.UserJpaRepository;
import com.diabecare.infrastructure.security.jwt.JwtService;
import com.diabecare.presentation.dto.request.LoginRequest;
import com.diabecare.presentation.dto.request.RegisterRequest;
import com.diabecare.presentation.dto.response.AuthResponse;
import com.diabecare.presentation.dto.response.PatientResponse;
import com.diabecare.presentation.mapper.PatientPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final RegisterPatientUseCase registerPatientUseCase;
    private final GetPatientUseCase getPatientUseCase;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserJpaRepository userJpaRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PatientPresentationMapper patientMapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserRecord user = registerUserUseCase.execute(
                new RegisterUserUseCase.Command(request.email(), request.password()));

        Patient patient = registerPatientUseCase.execute(
                new RegisterPatientUseCase.Command(
                        user.id(),
                        request.fullName(),
                        LocalDate.parse(request.dateOfBirth()),
                        DiabetesType.valueOf(request.diabetesType()),
                        LocalDate.parse(request.diagnosisDate()),
                        new BigDecimal(request.heightCm())
                ));

        String token = generateToken(request.email());
        PatientResponse patientResponse = patientMapper.toResponse(patient);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(token, jwtProperties.getAccessTokenExpiryMs(), patientResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        String token = generateToken(request.email());

        var user = userJpaRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Patient patient = getPatientUseCase.getByUserId(user.getId());
        PatientResponse patientResponse = patientMapper.toResponse(patient);

        return ResponseEntity.ok(
                AuthResponse.of(token, jwtProperties.getAccessTokenExpiryMs(), patientResponse));
    }

    private String generateToken(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateAccessToken(userDetails);
    }
}