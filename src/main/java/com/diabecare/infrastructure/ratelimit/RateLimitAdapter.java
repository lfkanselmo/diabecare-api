package com.diabecare.infrastructure.ratelimit;

import com.diabecare.application.port.out.RateLimitPort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitAdapter implements RateLimitPort {

    private final Cache<UUID, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Override
    public boolean tryConsume(String operationKey, UUID patientId, int limitPerHour) {
        String cacheKey = patientId + ":" + operationKey;
        UUID bucketKey = UUID.nameUUIDFromBytes(cacheKey.getBytes());

        Bucket bucket = buckets.get(bucketKey, k -> buildBucket(limitPerHour));

        return bucket.tryConsume(1);
    }

    private Bucket buildBucket(int limitPerHour) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limitPerHour)
                        .refillGreedy(limitPerHour, Duration.ofHours(1))
                        .build())
                .build();
    }
}