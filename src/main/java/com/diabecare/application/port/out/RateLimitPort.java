package com.diabecare.application.port.out;

import java.util.UUID;

public interface RateLimitPort {
    boolean tryConsume(String operationKey, UUID patientId, int limitPerHour);
}