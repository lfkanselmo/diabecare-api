package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SavePatientPort;
import com.diabecare.domain.model.Patient;
import com.diabecare.infrastructure.persistence.mapper.PatientPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.PatientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PatientPersistenceAdapter implements SavePatientPort, LoadPatientPort {

    private final PatientJpaRepository repository;
    private final PatientPersistenceMapper mapper;

    @Override
    public Patient save(Patient patient) {
        return mapper.toDomain(repository.save(mapper.toEntity(patient)));
    }

    @Override
    public Optional<Patient> findById(UUID patientId) {
        return repository.findById(patientId).map(mapper::toDomain);
    }

    @Override
    public Optional<Patient> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return repository.existsByUserId(userId);
    }
}