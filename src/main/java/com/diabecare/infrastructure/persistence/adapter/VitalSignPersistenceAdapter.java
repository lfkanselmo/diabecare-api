package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadVitalSignPort;
import com.diabecare.application.port.out.SaveVitalSignPort;
import com.diabecare.domain.model.VitalSign;
import com.diabecare.infrastructure.persistence.mapper.VitalSignPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.VitalSignJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VitalSignPersistenceAdapter implements SaveVitalSignPort, LoadVitalSignPort {

    private final VitalSignJpaRepository repository;
    private final VitalSignPersistenceMapper mapper;

    @Override
    public VitalSign save(VitalSign vitalSign) {
        return mapper.toDomain(repository.save(mapper.toEntity(vitalSign)));
    }

    @Override
    public Optional<VitalSign> findById(UUID vitalId) {
        return repository.findById(vitalId).map(mapper::toDomain);
    }

    @Override
    public Optional<VitalSign> findLatestByPatientId(UUID patientId) {
        return repository.findFirstByPatientIdOrderByMeasuredAtDesc(patientId)
                .map(mapper::toDomain);
    }

    @Override
    public List<VitalSign> findByPatientId(UUID patientId) {
        return repository.findFirst500ByPatientIdOrderByMeasuredAtDesc(patientId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<VitalSign> findByPatientId(UUID patientId, Pageable pageable) {
        return repository.findByPatientIdOrderByMeasuredAtDesc(patientId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<VitalSign> findByPatientIdUpdatedAfter(UUID patientId, LocalDateTime since) {
        return repository.findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc(patientId, since)
                .stream().map(mapper::toDomain).toList();
    }
}