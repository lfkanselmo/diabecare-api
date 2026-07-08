package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RequestPasswordResetUseCase;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.PasswordResetTokenPort;
import com.diabecare.application.port.out.SendEmailPort;
import com.diabecare.domain.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RequestPasswordResetUseCaseImpl implements RequestPasswordResetUseCase {

    private static final long TOKEN_VALIDITY_HOURS = 1;

    private final LoadUserPort loadUserPort;
    private final PasswordResetTokenPort passwordResetTokenPort;
    private final SendEmailPort sendEmailPort;
    private final RateLimitService rateLimitService;

    @Override
    public void execute(Command command) {
        rateLimitService.checkForgotPasswordLimit(command.clientIp());

        // Siempre se retorna sin error exista o no la cuenta — decirle al llamador
        // "ese correo no existe" permitiría enumerar cuentas registradas.
        loadUserPort.findUserIdByEmail(command.email()).ifPresent(userId -> {
            var issued = passwordResetTokenPort.issue(userId, LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS));

            try {
                sendEmailPort.sendPasswordResetEmail(command.email(), issued.rawToken());
            } catch (Exception e) {
                // No se propaga: por la misma razón anti-enumeración de arriba,
                // esta operación nunca debe fallar visiblemente para el llamador.
                log.error("Error enviando correo de recuperación de contraseña: {}", e.getMessage());
            }
        });
    }
}
