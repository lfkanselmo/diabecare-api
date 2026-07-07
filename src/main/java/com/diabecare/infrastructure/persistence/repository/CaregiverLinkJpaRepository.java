package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.CaregiverLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaregiverLinkJpaRepository extends JpaRepository<CaregiverLinkEntity, UUID> {

    Optional<CaregiverLinkEntity> findById(UUID id);

    boolean existsByPatientIdAndCaregiverUserIdAndStatus(
            UUID patientId, UUID caregiverUserId, String status);

    List<CaregiverLinkEntity> findByPatientIdAndStatus(UUID patientId, String status);

    List<CaregiverLinkEntity> findByCaregiverUserIdAndStatus(UUID caregiverUserId, String status);
}
