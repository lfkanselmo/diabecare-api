package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.GlucoseReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface GlucoseReminderJpaRepository extends JpaRepository<GlucoseReminderEntity, UUID> {
    List<GlucoseReminderEntity> findByPatientIdOrderByReminderTime(UUID patientId);
    List<GlucoseReminderEntity> findByReminderTimeAndEnabledTrue(LocalTime reminderTime);
    void deleteByPatientId(UUID patientId);
}
