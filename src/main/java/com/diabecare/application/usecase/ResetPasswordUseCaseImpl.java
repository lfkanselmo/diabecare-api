package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ResetPasswordUseCase;
import com.diabecare.application.port.out.PasswordResetTokenPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.exception.InvalidPasswordResetTokenException;
import com.diabecare.domain.model.PasswordResetToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final PasswordResetTokenPort passwordResetTokenPort;
    private final SaveUserPort saveUserPort;
    private final RefreshTokenPort refreshTokenPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void execute(Command command) {
        PasswordResetToken token = passwordResetTokenPort.findByRawToken(command.rawToken())
                .filter(PasswordResetToken::isValid)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        saveUserPort.updatePassword(token.getUserId(), passwordEncoder.encode(command.newPassword()));
        passwordResetTokenPort.markUsed(token.getId());

        // Un cambio de contraseña debe cerrar cualquier sesión existente: si el
        // motivo del reset fue una cuenta comprometida, esto expulsa al atacante.
        refreshTokenPort.revokeAllForUser(token.getUserId());
    }
}
