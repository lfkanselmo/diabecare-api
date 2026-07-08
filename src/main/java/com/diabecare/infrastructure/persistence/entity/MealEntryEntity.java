package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meal_entries")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealEntryEntity {

    @Id
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    @Column(length = 500)
    private String notes;

    // BatchSize evita N+1 al paginar: la consulta paginada no puede usar JOIN
    // FETCH (Hibernate paginaría en memoria), así que items se carga en un
    // segundo SELECT por lote en vez de uno por cada meal_entry de la página.
    @OneToMany(mappedBy = "mealEntry", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    @BatchSize(size = 50)
    @Builder.Default
    private List<MealItemEntity> items = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}