package com.diabecare.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "diabecare")
public record DiabeCareProperties(
        Security security,
        Push     push
) {
    public record Security(
            String[] corsAllowedOrigins,
            int      bcryptStrength
    ) {}

    public record Push(
            String vapidPublicKey,
            String vapidPrivateKey,
            String vapidSubject
    ) {}
}