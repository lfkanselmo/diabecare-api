package com.diabecare.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "diabecare.jwt")
public class JwtProperties {

    @NotBlank
    private String secretKey;

    @Positive
    private long accessTokenExpiryMs = 900_000L;

    @Positive
    private long refreshTokenExpiryMs = 604_800_000L;
}