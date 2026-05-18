package com.diabecare.domain.model;

import com.diabecare.domain.exception.InvalidMealEntryException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MealEntry {

    private final UUID mealId;
    private final UUID patientId;
    private MealType mealType;
    private LocalDateTime consumedAt;
    private String notes;

    @Builder.Default
    private List<MealItem> items = new ArrayList<>();

    public static MealEntry create(
            UUID patientId,
            MealType mealType,
            LocalDateTime consumedAt,
            String notes
    ) {
        if (consumedAt == null) {
            throw new InvalidMealEntryException("La fecha de la comida es obligatoria");
        }
        if (consumedAt.isAfter(LocalDateTime.now())) {
            throw new InvalidMealEntryException("La fecha de la comida no puede ser futura");
        }

        return MealEntry.builder()
                .mealId(UUID.randomUUID())
                .patientId(patientId)
                .mealType(mealType)
                .consumedAt(consumedAt)
                .notes(notes)
                .items(new ArrayList<>())
                .build();
    }


    public void addItem(MealItem item) {
        if (item == null) {
            throw new InvalidMealEntryException("El alimento no puede ser nulo");
        }
        this.items.add(item);
    }

    public void removeItem(UUID mealItemId) {
        items.removeIf(i -> i.getMealItemId().equals(mealItemId));
    }

    public List<MealItem> getItems() {
        return Collections.unmodifiableList(items);
    }


    public BigDecimal getTotalCalories() {
        return items.stream()
                .map(MealItem::getCalories)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalCarbohydrates() {
        return items.stream()
                .map(MealItem::getCarbohydrates)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalProteins() {
        return items.stream()
                .map(MealItem::getProteins)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalFats() {
        return items.stream()
                .map(MealItem::getFats)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}