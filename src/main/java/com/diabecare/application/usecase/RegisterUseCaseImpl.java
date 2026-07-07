package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterPatientUseCase;
import com.diabecare.application.port.in.RegisterUseCase;
import com.diabecare.application.port.in.RegisterUserUseCase;
import com.diabecare.application.port.out.GenerateTokenPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.domain.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUseCaseImpl implements RegisterUseCase {

    private final RegisterUserUseCase    registerUserUseCase;
    private final RegisterPatientUseCase registerPatientUseCase;
    private final GenerateTokenPort      generateTokenPort;
    private final RefreshTokenPort       refreshTokenPort;
    private final RateLimitService       rateLimitService;

    @Override
    public Result execute(Command command) {
        rateLimitService.checkRegisterLimit(command.clientIp());

        var user = registerUserUseCase.execute(
                new RegisterUserUseCase.Command(command.email(), command.password(), command.termsVersion()));

        var patient = registerPatientUseCase.execute(
                new RegisterPatientUseCase.Command(
                        user.id(),
                        command.fullName(),
                        command.dateOfBirth(),
                        command.diabetesType(),
                        command.diagnosisDate(),
                        command.heightCm(),
                        command.biologicalSex()
                ));

        String token = generateTokenPort.generateToken(command.email(), user.id());
        var refreshToken = refreshTokenPort.issue(user.id(), command.deviceLabel());

        return new Result(
                token,
                generateTokenPort.getExpiresIn(),
                refreshToken.rawToken(),
                refreshToken.expiresInMs(),
                patient.getPatientId().toString(),
                user.id().toString()
        );
    }
}