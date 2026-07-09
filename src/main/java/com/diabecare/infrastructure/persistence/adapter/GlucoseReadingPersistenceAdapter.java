package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.SaveGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.infrastructure.persistence.mapper.GlucoseReadingPersistenceMapper;
import com.diabecare.infrastructure.persistence.repository.GlucoseReadingJpaRepository;
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
public class GlucoseReadingPersistenceAdapter implements SaveGlucoseReadingPort, LoadGlucoseReadingPort {

    private final GlucoseReadingJpaRepository repository;
    private final GlucoseReadingPersistenceMapper mapper;

    @Override
    public GlucoseReading save(GlucoseReading reading) {
        return mapper.toDomain(repository.save(mapper.toEntity(reading)));
    }

    @Override
    public Optional<GlucoseReading> findById(UUID readingId) {
        return repository.findById(readingId).map(mapper::toDomain);
    }

    @Override
    public Optional<GlucoseReading> findLatestByPatientId(UUID patientId) {
        return repository.findFirstByPatientIdOrderByMeasuredAtDesc(patientId)
                .map(mapper::toDomain);
    }

    @Override
    public List<GlucoseReading> findByPatientIdAndDateRange(UUID patientId,
                                                            LocalDateTime from,
                                                            LocalDateTime to) {
        return repository.findByPatientIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
                        patientId, from, to)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<GlucoseReading> findByPatientIdAndDateRange(UUID patientId,
                                                            LocalDateTime from,
                                                            LocalDateTime to,
                                                            Pageable pageable) {
        return repository.findByPatientIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
                        patientId, from, to, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID readingId) {
        repository.deleteById(readingId);
    }

    @Override
    public List<GlucoseReading> findByPatientIdUpdatedAfter(UUID patientId, LocalDateTime since) {
        return repository.findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc(patientId, since)
                .stream().map(mapper::toDomain).toList();
    }
}