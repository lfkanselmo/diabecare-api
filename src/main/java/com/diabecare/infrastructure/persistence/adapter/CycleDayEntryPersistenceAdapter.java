package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadCycleDayEntryPort;
import com.diabecare.application.port.out.SaveCycleDayEntryPort;
import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CycleSymptomEntry;
import com.diabecare.infrastructure.persistence.entity.CycleDayEntryEntity;
import com.diabecare.infrastructure.persistence.entity.CycleDaySymptomEntity;
import com.diabecare.infrastructure.persistence.mapper.CycleDayEntryPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.CycleDayEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CycleDayEntryPersistenceAdapter implements SaveCycleDayEntryPort, LoadCycleDayEntryPort {

    private final CycleDayEntryJpaRepository repository;
    private final CycleDayEntryPersistenceMapper mapper;

    @Override
    public CycleDayEntry save(CycleDayEntry entry) {
        CycleDayEntryEntity entity = mapper.toEntity(entry);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }

        List<CycleDaySymptomEntity> symptomEntities = entry.getSymptoms().stream()
                .map(s -> CycleDaySymptomEntity.builder()
                        .dayEntry(entity)
                        .symptomCode(s.getSymptom().name())
                        .severity(s.getSeverity().name())
                        .build())
                .toList();

        entity.getSymptoms().clear();
        entity.getSymptoms().addAll(symptomEntities);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<CycleDayEntry> findByCycleIdAndDate(UUID cycleId, LocalDate date) {
        return repository.findByCycleIdAndEntryDate(cycleId, date)
                .map(this::toDomainWithSymptoms);
    }

    @Override
    public List<CycleDayEntry> findByCycleId(UUID cycleId) {
        return repository.findByCycleIdOrderByEntryDate(cycleId).stream()
                .map(this::toDomainWithSymptoms)
                .toList();
    }

    @Override
    public List<CycleDayEntry> findByPatientIdAndDateRange(UUID patientId, LocalDate from, LocalDate to) {
        return repository.findByPatientIdAndEntryDateBetweenOrderByEntryDate(patientId, from, to).stream()
                .map(this::toDomainWithSymptoms)
                .toList();
    }

    private CycleDayEntry toDomainWithSymptoms(CycleDayEntryEntity entity) {
        CycleDayEntry domain = mapper.toDomain(entity);
        List<CycleSymptomEntry> symptoms = entity.getSymptoms().stream()
                .map(mapper::toSymptomDomain)
                .toList();

        return CycleDayEntry.builder()
                .dayEntryId(domain.getDayEntryId())
                .cycleId(domain.getCycleId())
                .patientId(domain.getPatientId())
                .entryDate(domain.getEntryDate())
                .flowIntensity(domain.getFlowIntensity())
                .notes(domain.getNotes())
                .symptoms(symptoms)
                .build();
    }
}