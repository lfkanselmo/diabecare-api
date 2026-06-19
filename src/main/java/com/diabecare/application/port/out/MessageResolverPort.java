package com.diabecare.application.port.out;

public interface MessageResolverPort {
    String resolve(String key, Object... args);
}