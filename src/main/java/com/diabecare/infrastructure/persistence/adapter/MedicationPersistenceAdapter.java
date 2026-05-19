package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.application.port.out.SaveMedicationPort;
import com.diabecare.domain.model.Medication;
import com.diabecare.infrastructure.persistence.mapper.MedicationPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.MedicationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MedicationPersistenceAdapter implements SaveMedicationPort, LoadMedicationPort {

    private final MedicationJpaRepository repository;
    private final MedicationPersistenceMapper mapper;

    @Override
    public Medication save(Medication medication) {
        return mapper.toDomain(repository.save(mapper.toEntity(medication)));
    }

    @Override
    public Optional<Medication> findById(UUID medicationId) {
        return repository.findById(medicationId).map(mapper::toDomain);
    }

    @Override
    public List<Medication> findActiveByPatientId(UUID patientId) {
        return repository.findByPatientIdAndActiveTrue(patientId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Medication> findAllByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId)
                .stream().map(mapper::toDomain).toList();
    }
}