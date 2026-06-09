package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.SaveMenstrualCyclePort;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.infrastructure.persistence.mapper.MenstrualCyclePersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.MenstrualCycleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MenstrualCyclePersistenceAdapter
        implements SaveMenstrualCyclePort, LoadMenstrualCyclePort {

    private final MenstrualCycleJpaRepository repository;
    private final MenstrualCyclePersistenceMapper mapper;

    @Override
    public MenstrualCycle save(MenstrualCycle cycle) {
        return mapper.toDomain(repository.save(mapper.toEntity(cycle)));
    }

    @Override
    public List<MenstrualCycle> findByPatientId(UUID patientId) {
        return repository.findByPatientIdOrderByCycleStartDateDesc(patientId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<MenstrualCycle> findLatestByPatientId(UUID patientId) {
        return repository.findFirstByPatientIdOrderByCycleStartDateDesc(patientId)
                .map(mapper::toDomain);
    }
}