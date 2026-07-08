package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetActiveSessionsUseCase;
import com.diabecare.application.port.in.GetPatientUseCase;
import com.diabecare.application.port.in.LoginUseCase;
import com.diabecare.application.port.in.LogoutAllSessionsUseCase;
import com.diabecare.application.port.in.LogoutCurrentSessionUseCase;
import com.diabecare.application.port.in.RefreshAccessTokenUseCase;
import com.diabecare.application.port.in.RegisterUseCase;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.presentation.dto.request.LoginRequest;
import com.diabecare.presentation.dto.request.LogoutCurrentSessionRequest;
import com.diabecare.presentation.dto.request.LogoutRequest;
import com.diabecare.presentation.dto.request.RefreshTokenRequest;
import com.diabecare.presentation.dto.request.RegisterRequest;
import com.diabecare.presentation.dto.response.ActiveSessionResponse;
import com.diabecare.presentation.dto.response.AuthResponse;
import com.diabecare.presentation.dto.response.RefreshTokenResponse;
import com.diabecare.presentation.mapper.PatientPresentationMapper;
import com.diabecare.presentation.util.CurrentUserResolver;
import com.diabecare.presentation.util.DeviceLabelResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación")
public class AuthController {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    // Versión de la política de tratamiento de datos vigente al momento del
    // registro (Ley 1581 de 2012) — súbela si el texto de la política cambia
    // de forma sustancial, para poder distinguir quién aceptó qué versión.
    private static final String CURRENT_TERMS_VERSION = "2026-07";

    private final LoginUseCase                loginUseCase;
    private final RegisterUseCase             registerUseCase;
    private final RefreshAccessTokenUseCase   refreshAccessTokenUseCase;
    private final LogoutCurrentSessionUseCase logoutCurrentSessionUseCase;
    private final LogoutAllSessionsUseCase    logoutAllSessionsUseCase;
    private final GetActiveSessionsUseCase    getActiveSessionsUseCase;
    private final GetPatientUseCase           getPatientUseCase;
    private final PatientPresentationMapper   patientMapper;
    private final CurrentUserResolver         currentUserResolver;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario y paciente")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        RegisterUseCase.Result result = registerUseCase.execute(
                new RegisterUseCase.Command(
                        request.email(),
                        request.password(),
                        request.fullName(),
                        LocalDate.parse(request.dateOfBirth()),
                        DiabetesType.valueOf(request.diabetesType()),
                        LocalDate.parse(request.diagnosisDate()),
                        request.heightCm() != null ? new BigDecimal(request.heightCm()) : null,
                        BiologicalSex.valueOf(request.biologicalSex()),
                        deviceLabel(httpRequest),
                        clientIp(httpRequest),
                        CURRENT_TERMS_VERSION
                ));

        var patient = getPatientUseCase.getByUserId(UUID.fromString(result.userId()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(
                        result.token(),
                        result.expiresIn(),
                        result.refreshToken(),
                        result.refreshExpiresIn(),
                        patientMapper.toResponse(patient.patient()),
                        result.role()
                ));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        LoginUseCase.Result result = loginUseCase.execute(
                new LoginUseCase.Command(request.email(), request.password(),
                        deviceLabel(httpRequest), clientIp(httpRequest)));

        var patient = getPatientUseCase.getByUserId(UUID.fromString(result.userId()));

        return ResponseEntity.ok(AuthResponse.of(
                result.token(),
                result.expiresIn(),
                result.refreshToken(),
                result.refreshExpiresIn(),
                patientMapper.toResponse(patient.patient()),
                result.role()
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Obtener un nuevo access token a partir de un refresh token vigente")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshAccessTokenUseCase.Result result = refreshAccessTokenUseCase.execute(
                new RefreshAccessTokenUseCase.Command(request.refreshToken()));

        return ResponseEntity.ok(RefreshTokenResponse.of(
                result.accessToken(),
                result.accessTokenExpiresIn(),
                result.refreshToken(),
                result.refreshTokenExpiresIn()
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión solo en el dispositivo actual")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutCurrentSessionRequest request) {
        logoutCurrentSessionUseCase.execute(new LogoutCurrentSessionUseCase.Command(request.refreshToken()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Cerrar sesión en todos los dispositivos")
    public ResponseEntity<Void> logoutAll(@Valid @RequestBody LogoutRequest request,
                                          Authentication authentication) {
        currentUserResolver.verifyIsCurrentUser(request.userId(), authentication);
        logoutAllSessionsUseCase.execute(new LogoutAllSessionsUseCase.Command(request.userId()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{userId}")
    @Operation(summary = "Listar las sesiones (dispositivos) activas del usuario")
    public ResponseEntity<List<ActiveSessionResponse>> getActiveSessions(
            @PathVariable UUID userId, Authentication authentication) {
        currentUserResolver.verifyIsCurrentUser(userId, authentication);

        var sessions = getActiveSessionsUseCase.execute(userId).stream()
                .map(s -> new ActiveSessionResponse(s.id(), s.deviceLabel(), s.lastUsedAt(), s.createdAt()))
                .toList();

        return ResponseEntity.ok(sessions);
    }

    private String deviceLabel(HttpServletRequest request) {
        return DeviceLabelResolver.resolve(request.getHeader(USER_AGENT_HEADER));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}