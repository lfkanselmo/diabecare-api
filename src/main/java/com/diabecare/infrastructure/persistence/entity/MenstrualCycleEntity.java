package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "menstrual_cycles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenstrualCycleEntity {

    @Id
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "cycle_start_date", nullable = false)
    private LocalDate cycleStartDate;

    @Column(name = "cycle_end_date")
    private LocalDate cycleEndDate;

    @Column(name = "cycle_length_days")
    private Integer cycleLengthDays;

    @Column(name = "period_length_days")
    private Integer periodLengthDays;

    @Column(nullable = false, length = 20)
    private String phase;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(length = 500)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}