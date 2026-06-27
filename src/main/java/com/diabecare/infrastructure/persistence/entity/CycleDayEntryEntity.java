package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cycle_day_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleDayEntryEntity {

    @Id
    private UUID id;

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "flow_intensity", nullable = false, length = 20)
    private String flowIntensity;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dayEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<CycleDaySymptomEntity> symptoms = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}