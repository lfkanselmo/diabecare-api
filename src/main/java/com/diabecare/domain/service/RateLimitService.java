package com.diabecare.domain.service;

import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.exception.RateLimitExceededException;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final Cache<UUID, Bucket> rateLimitCache;
    private final SystemConfigPort    systemConfig;

    public void checkGlucoseLimit(UUID patientId) {
        checkLimit(patientId, "GLUCOSE",
                systemConfig.getInt("rate_limit.glucose_per_hour"));
    }

    public void checkMealLimit(UUID patientId) {
        checkLimit(patientId, "MEAL",
                systemConfig.getInt("rate_limit.meal_per_hour"));
    }

    public void checkExerciseLimit(UUID patientId) {
        checkLimit(patientId, "EXERCISE",
                systemConfig.getInt("rate_limit.exercise_per_hour"));
    }

    private void checkLimit(UUID patientId, String operation, int limit) {
        String cacheKey  = patientId + ":" + operation;
        UUID   bucketKey = UUID.nameUUIDFromBytes(cacheKey.getBytes());

        Bucket bucket = rateLimitCache.get(bucketKey, k -> buildBucket(limit));

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(
                    "Límite de registros excedido para " + operation +
                            ". Intenta de nuevo más tarde.");
        }
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