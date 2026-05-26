package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "calories_per_100g", nullable = false, precision = 8, scale = 2)
    private BigDecimal caloriesPer100g;

    @Column(name = "carbs_per_100g", nullable = false, precision = 8, scale = 2)
    private BigDecimal carbsPer100g;

    @Column(name = "proteins_per_100g", nullable = false, precision = 8, scale = 2)
    private BigDecimal proteinsPer100g;

    @Column(name = "fats_per_100g", nullable = false, precision = 8, scale = 2)
    private BigDecimal fatsPer100g;

    @Column(name = "fiber_per_100g", precision = 8, scale = 2)
    private BigDecimal fiberPer100g;

    @Column(name = "sodium_per_100g", precision = 8, scale = 2)
    private BigDecimal sodiumPer100g;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}