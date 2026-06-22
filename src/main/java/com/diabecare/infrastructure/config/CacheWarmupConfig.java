package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.LoadFoodPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CacheWarmupConfig {

    private final LoadFoodPort loadFoodPort;

    private static final List<String> FOOD_CATEGORIES = List.of(
            "CEREALES", "LEGUMBRES", "TUBERCULOS", "VERDURAS",
            "FRUTAS", "CARNES", "PESCADOS", "LACTEOS",
            "GRASAS", "AZUCARES", "BEBIDAS", "PREPARADOS",
            "FRUTOS_SECOS", "EMBUTIDOS", "CONDIMENTOS", "COMIDA_RAPIDA",
            "PANADERIA", "VEGANOS", "INDUSTRIALES", "COMIDA_CALLE"
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
}