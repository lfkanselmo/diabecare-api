package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadExerciseLogPort;
import com.diabecare.application.port.out.SaveExerciseLogPort;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.infrastructure.persistence.entity.ExerciseLogEntity;
import com.diabecare.infrastructure.persistence.mapper.ExerciseLogPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.ExerciseLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExerciseLogPersistenceAdapter implements SaveExerciseLogPort, LoadExerciseLogPort {

    private final ExerciseLogJpaRepository repository;
    private final ExerciseLogPersistenceMapper mapper;

    @Override
    public ExerciseLog save(ExerciseLog exerciseLog) {
        ExerciseLogEntity entity = mapper.toEntity(exerciseLog);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<ExerciseLog> findByPatientIdAndDateRange(UUID patientId,
                                                         LocalDateTime from,
                                                         LocalDateTime to) {
        return repository
                .findByPatientIdAndPerformedAtBetweenOrderByPerformedAtDesc(patientId, from, to)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<ExerciseLog> findByPatientIdAndDateRange(UUID patientId,
                                                         LocalDateTime from,
                                                         LocalDateTime to,
                                                         Pageable pageable) {
        return repository
                .findByPatientIdAndPerformedAtBetweenOrderByPerformedAtDesc(patientId, from, to, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<ExerciseLog> findByPatientIdUpdatedAfter(UUID patientId, LocalDateTime since) {
        return repository.findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc(patientId, since)
                .stream().map(mapper::toDomain).toList();
    }
}