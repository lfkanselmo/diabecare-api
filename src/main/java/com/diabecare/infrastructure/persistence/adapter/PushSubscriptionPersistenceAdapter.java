package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.PushSubscriptionPort;
import com.diabecare.domain.model.PushSubscription;
import com.diabecare.infrastructure.persistence.entity.PushSubscriptionEntity;
import com.diabecare.infrastructure.persistence.mapper.PushSubscriptionPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.PushSubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PushSubscriptionPersistenceAdapter implements PushSubscriptionPort {

    private final PushSubscriptionJpaRepository jpaRepository;
    private final PushSubscriptionPersistenceMapper mapper;

    @Override
    public boolean existsByPatientIdAndEndpoint(UUID patientId, String endpoint) {
        return jpaRepository.findAllByPatientId(patientId).stream()
                .anyMatch(s -> s.getEndpoint().equals(endpoint));
    }

    @Override
    public void save(UUID patientId, String endpoint, String p256dh, String auth) {
        jpaRepository.save(PushSubscriptionEntity.builder()
                .patientId(patientId)
                .endpoint(endpoint)
                .p256dh(p256dh)
                .auth(auth)
                .build());
    }

    @Override
    public void deleteByEndpoint(String endpoint) {
        jpaRepository.deleteByEndpoint(endpoint);
    }

    @Override
    public List<PushSubscription> findAllByPatientId(UUID patientId) {
        return jpaRepository.findAllByPatientId(patientId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}