package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CaregiverLink {

    private UUID              id;
    private UUID              patientId;
    private UUID              caregiverUserId;
    private CaregiverLinkStatus status;
    private LocalDateTime     createdAt;
    private LocalDateTime     revokedAt;

    public static CaregiverLink create(UUID patientId, UUID caregiverUserId) {
        return CaregiverLink.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .caregiverUserId(caregiverUserId)
                .status(CaregiverLinkStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isActive() {
        return status == CaregiverLinkStatus.ACTIVE;
    }

    public void revoke() {
        this.status = CaregiverLinkStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * Reactiva un vínculo previamente revocado (el paciente vuelve a invitar a un
     * cuidador que ya había quitado) — reutiliza la fila existente en vez de crear
     * una nueva, porque (patientId, caregiverUserId) tiene una restricción de
     * unicidad en la base de datos.
     */
    public void reactivate() {
        this.status = CaregiverLinkStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.revokedAt = null;
    }
}
