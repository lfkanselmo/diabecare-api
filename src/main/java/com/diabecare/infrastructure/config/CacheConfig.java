package com.diabecare.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCache foodsCache = new CaffeineCache("foods",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .recordStats()
                        .build());

        // TTL más largo: los datos de un producto empacado (Open Food Facts) cambian
        // con mucha menor frecuencia que el catálogo propio de alimentos genéricos.
        CaffeineCache openFoodFactsLookupCache = new CaffeineCache("openFoodFactsLookup",
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(7, TimeUnit.DAYS)
                        .recordStats()
                        .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(foodsCache, openFoodFactsLookupCache));
        return manager;
    }
}