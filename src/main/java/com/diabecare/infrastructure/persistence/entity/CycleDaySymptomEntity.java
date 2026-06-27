package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cycle_day_symptoms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleDaySymptomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_entry_id", nullable = false)
    private CycleDayEntryEntity dayEntry;

    @Column(name = "symptom_code", nullable = false, length = 30)
    private String symptomCode;

    @Column(nullable = false, length = 10)
    private String severity;
}