package com.diabecare.infrastructure.config;

import com.diabecare.domain.service.MedicalCalculatorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public MedicalCalculatorService medicalCalculatorService() {
        return new MedicalCalculatorService();
    }
}