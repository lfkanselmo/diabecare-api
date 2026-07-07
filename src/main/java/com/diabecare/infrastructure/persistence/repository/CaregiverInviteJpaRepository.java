package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.CaregiverInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CaregiverInviteJpaRepository extends JpaRepository<CaregiverInviteEntity, UUID> {
    Optional<CaregiverInviteEntity> findByCodeHash(String codeHash);

    void deleteByPatientId(UUID patientId);

    // Cuando se purga una cuenta que en su momento redimió la invitación de OTRO
    // paciente, no borramos esa invitación (pertenece al paciente que la creó) —
    // solo desvinculamos la referencia para no romper la FK contra users(id).
    @Modifying
    @Query("UPDATE CaregiverInviteEntity c SET c.redeemedByUserId = NULL WHERE c.redeemedByUserId = :userId")
    void clearRedeemedByUserId(@Param("userId") UUID userId);
}
