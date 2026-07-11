package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.SaveCaregiverLinkPort;
import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.model.CaregiverLinkStatus;
import com.diabecare.infrastructure.persistence.entity.CaregiverLinkEntity;
import com.diabecare.infrastructure.persistence.repository.CaregiverLinkJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CaregiverLinkPersistenceAdapter implements SaveCaregiverLinkPort, LoadCaregiverLinkPort {

    private final CaregiverLinkJpaRepository caregiverLinkJpaRepository;

    @Override
    @Transactional
    public CaregiverLink save(CaregiverLink link) {
        return toDomain(caregiverLinkJpaRepository.save(toEntity(link)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CaregiverLink> findById(UUID linkId) {
        return caregiverLinkJpaRepository.findById(linkId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActive(UUID patientId, UUID caregiverUserId) {
        return caregiverLinkJpaRepository.existsByPatientIdAndCaregiverUserIdAndStatus(
                patientId, caregiverUserId, CaregiverLinkStatus.ACTIVE.name());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CaregiverLink> findByPatientIdAndCaregiverUserId(UUID patientId, UUID caregiverUserId) {
        return caregiverLinkJpaRepository
                .findByPatientIdAndCaregiverUserId(patientId, caregiverUserId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaregiverLink> findActiveByPatientId(UUID patientId) {
        return caregiverLinkJpaRepository
                .findByPatientIdAndStatus(patientId, CaregiverLinkStatus.ACTIVE.name())
                .stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaregiverLink> findActiveByCaregiverUserId(UUID caregiverUserId) {
        return caregiverLinkJpaRepository
                .findByCaregiverUserIdAndStatus(caregiverUserId, CaregiverLinkStatus.ACTIVE.name())
                .stream().map(this::toDomain).toList();
    }

    private CaregiverLinkEntity toEntity(CaregiverLink link) {
        return CaregiverLinkEntity.builder()
                .id(link.getId())
                .patientId(link.getPatientId())
                .caregiverUserId(link.getCaregiverUserId())
                .status(link.getStatus().name())
                .createdAt(link.getCreatedAt())
                .revokedAt(link.getRevokedAt())
                .build();
    }

    private CaregiverLink toDomain(CaregiverLinkEntity entity) {
        return CaregiverLink.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .caregiverUserId(entity.getCaregiverUserId())
                .status(CaregiverLinkStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .revokedAt(entity.getRevokedAt())
                .build();
    }
}
