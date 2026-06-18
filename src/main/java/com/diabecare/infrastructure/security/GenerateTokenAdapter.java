package com.diabecare.infrastructure.security;

import com.diabecare.application.port.out.GenerateTokenPort;
import com.diabecare.infrastructure.config.JwtProperties;
import com.diabecare.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GenerateTokenAdapter implements GenerateTokenPort {

    private final JwtService         jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtProperties      jwtProperties;

    @Override
    public String generateToken(String email, UUID userId) {
        var userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateAccessToken(userDetails, userId);
    }

    @Override
    public long getExpiresIn() {
        return jwtProperties.getAccessTokenExpiryMs();
    }
}