package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.RateLimitPort;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitPort    rateLimitPort;
    private final SystemConfigPort systemConfig;

    @Bean
    public RateLimitService rateLimitService() {
        return new RateLimitService(rateLimitPort, systemConfig);
    }
}