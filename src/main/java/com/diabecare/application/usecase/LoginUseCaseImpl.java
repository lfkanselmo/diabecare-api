package com.diabecare.application.usecase;

import com.diabecare.application.port.in.LoginUseCase;
import com.diabecare.application.port.out.AuthenticateUserPort;
import com.diabecare.application.port.out.GenerateTokenPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.domain.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final AuthenticateUserPort authenticateUserPort;
    private final LoadUserPort         loadUserPort;
    private final LoadPatientPort      loadPatientPort;
    private final GenerateTokenPort    generateTokenPort;
    private final RefreshTokenPort     refreshTokenPort;
    private final RateLimitService     rateLimitService;

    @Override
    public Result execute(Command command) {
        rateLimitService.checkLoginLimit(command.clientIp());

        authenticateUserPort.authenticate(command.email(), command.password());

        var userId = loadUserPort.findUserIdByEmail(command.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var patient = loadPatientPort.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        String token = generateTokenPort.generateToken(command.email(), userId);
        var refreshToken = refreshTokenPort.issue(userId, command.deviceLabel());

        return new Result(
                token,
                generateTokenPort.getExpiresIn(),
                refreshToken.rawToken(),
                refreshToken.expiresInMs(),
                patient.getPatientId().toString(),
                userId.toString()
        );
    }
}