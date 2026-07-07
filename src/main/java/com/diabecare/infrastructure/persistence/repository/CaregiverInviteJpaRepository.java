package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.CaregiverInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CaregiverInviteJpaRepository extends JpaRepository<CaregiverInviteEntity, UUID> {
    Optional<CaregiverInviteEntity> findByCodeHash(String codeHash);
}
