package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.LoadFoodPort;
import com.diabecare.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CacheWarmupConfig {

    private final LoadFoodPort loadFoodPort;
    private final CacheManager cacheManager;

    private static final List<String> FOOD_CATEGORIES = List.of(
            "CEREALES", "LEGUMBRES", "TUBERCULOS", "VERDURAS",
            "FRUTAS", "CARNES", "PESCADOS", "LACTEOS",
            "GRASAS", "AZUCARES", "BEBIDAS", "PREPARADOS"
    );

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        warmUpFoods();
    }

    private void warmUpFoods() {
        log.info("Precargando caché de alimentos...");
        long start = System.currentTimeMillis();
        FOOD_CATEGORIES.forEach(category -> {
            try {
                loadFoodPort.findByCategory(category);
            } catch (Exception e) {
                log.warn("Error precargando categoría {}: {}", category, e.getMessage());
            }
        });
        log.info("Caché de alimentos precargada en {} ms",
                System.currentTimeMillis() - start);
    }

    private <T extends Enum<T>> List<Map<String, String>> buildList(
            T[] values, java.util.function.Function<T, String> labelFn) {
        return Arrays.stream(values)
                .map(v -> Map.of("value", v.name(), "label", labelFn.apply(v)))
                .toList();
    }

    private String exerciseLabel(ExerciseType t) {
        return switch (t) {
            case WALKING         -> "Caminata";
            case RUNNING         -> "Trote / Carrera";
            case CYCLING         -> "Ciclismo";
            case SWIMMING        -> "Natación";
            case WEIGHT_TRAINING -> "Pesas";
            case YOGA            -> "Yoga";
            case FOOTBALL        -> "Fútbol";
            case BASKETBALL      -> "Baloncesto";
            case DANCING         -> "Baile";
            case HIKING          -> "Senderismo";
            case OTHER           -> "Otro";
        };
    }

    private String intensityLabel(ExerciseIntensity i) {
        return switch (i) {
            case LOW      -> "Baja";
            case MODERATE -> "Moderada";
            case HIGH     -> "Alta";
        };
    }

    private String medicationTypeLabel(MedicationType t) {
        return switch (t) {
            case INSULIN_BASAL -> "Insulina basal";
            case INSULIN_BOLUS -> "Insulina bolo";
            case ORAL          -> "Medicamento oral";
            case INJECTABLE    -> "Inyectable";
        };
    }

    private String doseUnitLabel(DoseUnit u) {
        return switch (u) {
            case MG    -> "mg";
            case ML    -> "mL";
            case UNITS -> "Unidades";
        };
    }

    private String frequencyLabel(MedicationFrequency f) {
        return switch (f) {
            case ONCE_DAILY        -> "Una vez al día";
            case TWICE_DAILY       -> "Dos veces al día";
            case THREE_TIMES_DAILY -> "Tres veces al día";
            case WITH_MEALS        -> "Con las comidas";
            case BEFORE_MEALS      -> "Antes de comidas";
            case AT_BEDTIME        -> "Al acostarse";
            case AS_NEEDED         -> "Según necesidad";
        };
    }

    private String mealTypeLabel(MealType m) {
        return switch (m) {
            case BREAKFAST -> "Desayuno";
            case LUNCH     -> "Almuerzo";
            case DINNER    -> "Cena";
            case SNACK     -> "Merienda";
        };
    }

    private String readingTypeLabel(ReadingType r) {
        return switch (r) {
            case FASTING   -> "Ayuno";
            case PRE_MEAL  -> "Preprandial";
            case POST_MEAL -> "Postprandial";
            case BEDTIME   -> "Antes de dormir";
            case RANDOM    -> "Aleatoria";
        };
    }

    private String activityLevelLabel(ActivityLevel a) {
        return switch (a) {
            case SEDENTARY         -> "Sedentario";
            case LIGHTLY_ACTIVE    -> "Ligeramente activo";
            case MODERATELY_ACTIVE -> "Moderadamente activo";
            case VERY_ACTIVE       -> "Muy activo";
        };
    }

    private String diabetesTypeLabel(DiabetesType d) {
        return switch (d) {
            case TYPE_1      -> "Tipo 1";
            case TYPE_2      -> "Tipo 2";
            case GESTATIONAL -> "Gestacional";
            case LADA        -> "LADA";
            case MODY        -> "MODY";
        };
    }

    private String glucoseUnitLabel(GlucoseUnit g) {
        return switch (g) {
            case MG_DL  -> "mg/dL";
            case MMOL_L -> "mmol/L";
        };
    }
}