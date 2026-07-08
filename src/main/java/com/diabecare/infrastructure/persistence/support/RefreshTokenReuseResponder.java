package com.diabecare.infrastructure.persistence.support;

import com.diabecare.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Revoca todas las sesiones de un usuario en una transacción propia
 * (REQUIRES_NEW), separada de la transacción de {@code redeem()} que detectó
 * la reutilización. Es necesario: esa transacción termina lanzando
 * {@code InvalidRefreshTokenException} (el token sigue siendo inválido para
 * quien lo presentó), y Spring hace rollback de todo lo hecho en ella —
 * incluida la revocación masiva, si no vive en su propia transacción.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenReuseResponder {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllForUser(UUID userId) {
        refreshTokenJpaRepository.revokeAllActiveByUserId(userId, LocalDateTime.now());
    }
}
