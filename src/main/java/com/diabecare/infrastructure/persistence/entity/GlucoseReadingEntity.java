package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "glucose_readings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlucoseReadingEntity {

    @Id
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal value;

    @Column(nullable = false, length = 10)
    private String unit;

    @Column(name = "reading_type", nullable = false, length = 20)
    private String readingType;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(length = 500)
    private String notes;

    @Column(name = "device_source", length = 100)
    private String deviceSource;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}