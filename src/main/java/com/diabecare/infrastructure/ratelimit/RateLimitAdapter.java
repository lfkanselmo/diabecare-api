package com.diabecare.infrastructure.ratelimit;

import com.diabecare.application.port.out.RateLimitPort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitAdapter implements RateLimitPort {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Override
    public boolean tryConsume(String operationKey, String subjectKey, int limitPerHour) {
        String cacheKey = operationKey + ":" + subjectKey;

        Bucket bucket = buckets.get(cacheKey, k -> buildBucket(limitPerHour));

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