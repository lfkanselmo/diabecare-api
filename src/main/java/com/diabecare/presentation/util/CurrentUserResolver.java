package com.diabecare.presentation.util;

import com.diabecare.application.port.out.LoadCaregiverLinkPort;
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

    private static final String NO_ACCESS_MESSAGE = "No tienes permiso para acceder a este recurso";

    private final LoadUserPort loadUserPort;
    private final LoadPatientPort loadPatientPort;
    private final LoadCaregiverLinkPort loadCaregiverLinkPort;

    public UUID resolveUserId(Authentication authentication) {
        String email = extractEmail(authentication);
        return loadUserPort.findUserIdByEmail(email)
                .orElseThrow(() -> new UnauthorizedResourceAccessException(
                        "No se pudo identificar al usuario autenticado"));
    }

    public void verifyOwnsPatient(UUID patientId, Authentication authentication) {
        UUID currentUserId = resolveUserId(authentication);

        UUID patientOwnerId = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException(NO_ACCESS_MESSAGE))
                .getUserId();

        if (!patientOwnerId.equals(currentUserId)) {
            throw new UnauthorizedResourceAccessException(NO_ACCESS_MESSAGE);
        }
    }

    /**
     * Permite el acceso de solo lectura al dueño del paciente O a un cuidador con
     * un enlace activo (ver Fase 3: compartir con cuidadores) — a diferencia de
     * {@link #verifyOwnsPatient}, que solo debe usarse en endpoints de escritura.
     */
    public void verifyCanReadPatient(UUID patientId, Authentication authentication) {
        UUID currentUserId = resolveUserId(authentication);

        UUID patientOwnerId = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException(NO_ACCESS_MESSAGE))
                .getUserId();

        if (patientOwnerId.equals(currentUserId)) {
            return;
        }

        if (!loadCaregiverLinkPort.existsActive(patientId, currentUserId)) {
            throw new UnauthorizedResourceAccessException(NO_ACCESS_MESSAGE);
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