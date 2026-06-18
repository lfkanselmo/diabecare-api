package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.SystemConfigPort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final SystemConfigPort systemConfig;

    @Bean
    public Cache<UUID, Bucket> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
    }

    private Bucket buildBucket(int limit) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit, Duration.ofHours(1))
                        .build())
                .build();
    }
}