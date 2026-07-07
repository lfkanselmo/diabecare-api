package com.diabecare.application.port.out;

public interface RateLimitPort {
    boolean tryConsume(String operationKey, String subjectKey, int limitPerHour);
}