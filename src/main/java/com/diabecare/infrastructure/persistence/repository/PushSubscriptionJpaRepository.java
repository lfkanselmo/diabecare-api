package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.PushSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PushSubscriptionJpaRepository extends JpaRepository<PushSubscriptionEntity, UUID> {
    List<PushSubscriptionEntity> findAllByPatientId(UUID patientId);
    void deleteByEndpoint(String endpoint);
}