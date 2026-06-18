package com.diabecare.application.usecase;

import com.diabecare.application.port.in.LoginUseCase;
import com.diabecare.application.port.out.AuthenticateUserPort;
import com.diabecare.application.port.out.GenerateTokenPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final AuthenticateUserPort authenticateUserPort;
    private final LoadUserPort         loadUserPort;
    private final LoadPatientPort      loadPatientPort;
    private final GenerateTokenPort    generateTokenPort;

    @Override
    public Result execute(Command command) {
        authenticateUserPort.authenticate(command.email(), command.password());

        var userId = loadUserPort.findUserIdByEmail(command.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var patient = loadPatientPort.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        String token = generateTokenPort.generateToken(command.email(), userId);

        return new Result(
                token,
                generateTokenPort.getExpiresIn(),
                patient.getPatientId().toString(),
                userId.toString()
        );
    }
}