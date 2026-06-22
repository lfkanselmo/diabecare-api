package com.diabecare.presentation.util;

import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final LoadUserPort loadUserPort;
    private final LoadPatientPort loadPatientPort;

    public UUID resolveUserId(Authentication authentication) {
        String email = extractEmail(authentication);
        return loadUserPort.findUserIdByEmail(email)
                .orElseThrow(() -> new UnauthorizedResourceAccessException(
                        "No se pudo identificar al usuario autenticado"));
    }

    public void verifyOwnsPatient(UUID patientId, Authentication authentication) {
        UUID currentUserId = resolveUserId(authentication);

        UUID patientOwnerId = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException(
                        "No tienes permiso para acceder a este recurso"))
                .getUserId();

        if (!patientOwnerId.equals(currentUserId)) {
            throw new UnauthorizedResourceAccessException(
                    "No tienes permiso para acceder a este recurso");
        }
    }

    public void verifyIsCurrentUser(UUID userId, Authentication authentication) {
        UUID currentUserId = resolveUserId(authentication);

        if (!userId.equals(currentUserId)) {
            throw new UnauthorizedResourceAccessException(
                    "No tienes permiso para realizar esta acción");
        }
    }

    private String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }
}