package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.application.port.out.SaveMealEntryPort;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.infrastructure.persistence.entity.MealEntryEntity;
import com.diabecare.infrastructure.persistence.entity.MealItemEntity;
import com.diabecare.infrastructure.persistence.mapper.MealEntryPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.MealEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MealEntryPersistenceAdapter implements SaveMealEntryPort, LoadMealEntryPort {

    private final MealEntryJpaRepository repository;
    private final MealEntryPersistenceMapper mapper;

    @Override
    public MealEntry save(MealEntry mealEntry) {
        MealEntryEntity entity = mapper.toEntity(mealEntry);
        List<MealItemEntity> itemEntities = mealEntry.getItems().stream()
                .map(item -> buildItemEntity(item, entity))
                .toList();
        entity.setItems(itemEntities);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<MealEntry> findById(UUID mealId) {
        return repository.findById(mealId).map(mapper::toDomain);
    }

    @Override
    public List<MealEntry> findByPatientIdAndDate(UUID patientId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return repository.findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
                        patientId, start, end)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<MealEntry> findByPatientIdAndDateRange(UUID patientId,
                                                       LocalDate from,
                                                       LocalDate to) {
        return repository.findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
                        patientId, from.atStartOfDay(), to.atTime(23, 59, 59))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID mealId) {
        repository.deleteById(mealId);
    }

    private MealItemEntity buildItemEntity(MealItem item, MealEntryEntity parent) {
        MealItemEntity entity = mapper.toItemEntity(item);
        entity.setMealEntry(parent);
        return entity;
    }
}