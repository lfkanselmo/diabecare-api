package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadGlucoseReminderPort;
import com.diabecare.application.port.out.SaveGlucoseReminderPort;
import com.diabecare.domain.model.GlucoseReminder;
import com.diabecare.infrastructure.persistence.entity.GlucoseReminderEntity;
import com.diabecare.infrastructure.persistence.repository.GlucoseReminderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GlucoseReminderPersistenceAdapter implements LoadGlucoseReminderPort, SaveGlucoseReminderPort {

    private final GlucoseReminderJpaRepository repository;

    @Override
    public Optional<GlucoseReminder> findById(UUID reminderId) {
        return repository.findById(reminderId).map(this::toDomain);
    }

    @Override
    public List<GlucoseReminder> findByPatientId(UUID patientId) {
        return repository.findByPatientIdOrderByReminderTime(patientId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<GlucoseReminder> findAllEnabledAtTime(LocalTime time) {
        return repository.findByReminderTimeAndEnabledTrue(time)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public GlucoseReminder save(GlucoseReminder reminder) {
        GlucoseReminderEntity entity = GlucoseReminderEntity.builder()
                .id(reminder.getId())
                .patientId(reminder.getPatientId())
                .reminderTime(reminder.getReminderTime())
                .label(reminder.getLabel())
                .enabled(reminder.isEnabled())
                .createdAt(reminder.getCreatedAt() != null ? reminder.getCreatedAt() : LocalDateTime.now())
                .build();

        return toDomain(repository.save(entity));
    }

    @Override
    public void delete(UUID reminderId) {
        repository.deleteById(reminderId);
    }

    private GlucoseReminder toDomain(GlucoseReminderEntity entity) {
        return GlucoseReminder.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .reminderTime(entity.getReminderTime())
                .label(entity.getLabel())
                .enabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
