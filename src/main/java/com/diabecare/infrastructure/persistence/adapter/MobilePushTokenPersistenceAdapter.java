package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.MobilePushTokenPort;
import com.diabecare.domain.model.MobilePlatform;
import com.diabecare.domain.model.MobilePushToken;
import com.diabecare.infrastructure.persistence.entity.MobilePushTokenEntity;
import com.diabecare.infrastructure.persistence.mapper.MobilePushTokenPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.MobilePushTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MobilePushTokenPersistenceAdapter implements MobilePushTokenPort {

    private final MobilePushTokenJpaRepository jpaRepository;
    private final MobilePushTokenPersistenceMapper mapper;

    @Override
    public boolean existsByPatientIdAndDeviceToken(UUID patientId, String deviceToken) {
        return jpaRepository.findAllByPatientId(patientId).stream()
                .anyMatch(t -> t.getDeviceToken().equals(deviceToken));
    }

    @Override
    public void save(UUID patientId, String deviceToken, MobilePlatform platform) {
        jpaRepository.save(MobilePushTokenEntity.builder()
                .patientId(patientId)
                .deviceToken(deviceToken)
                .platform(platform.name())
                .build());
    }

    @Override
    public void deleteByDeviceToken(String deviceToken) {
        jpaRepository.deleteByDeviceToken(deviceToken);
    }

    @Override
    public List<MobilePushToken> findAllByPatientId(UUID patientId) {
        return jpaRepository.findAllByPatientId(patientId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
