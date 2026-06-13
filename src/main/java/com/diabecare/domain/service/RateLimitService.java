package com.diabecare.domain.service;

import com.diabecare.domain.exception.RateLimitExceededException;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final Cache<UUID, Bucket> rateLimitCache;

    public void checkLimit(UUID patientId, String operation, Supplier<Bucket> bucketSupplier) {
        String key = patientId.toString() + ":" + operation;
        UUID cacheKey = UUID.nameUUIDFromBytes(key.getBytes());

        Bucket bucket = rateLimitCache.get(cacheKey, k -> bucketSupplier.get());

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(
                    "Límite de registros excedido para " + operation +
                            ". Intenta de nuevo más tarde.");
        }
    }
}