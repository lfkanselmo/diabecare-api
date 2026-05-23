package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "meal_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealItemEntity implements Persistable<UUID>  {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_entry_id", nullable = false)
    private MealEntryEntity mealEntry;

    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;

    @Column(name = "quantity_grams", nullable = false, precision = 8, scale = 2)
    private BigDecimal quantityGrams;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal calories;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal carbohydrates;

    @Column(precision = 8, scale = 2)
    private BigDecimal proteins;

    @Column(precision = 8, scale = 2)
    private BigDecimal fats;

    @Column(name = "food_code", length = 50)
    private String foodCode;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}