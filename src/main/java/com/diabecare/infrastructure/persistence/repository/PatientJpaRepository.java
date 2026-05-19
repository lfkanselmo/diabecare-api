package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientJpaRepository extends JpaRepository<PatientEntity, UUID> {
    Optional<PatientEntity> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}