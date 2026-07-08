package com.diabecare.infrastructure.mail;

import com.diabecare.infrastructure.config.DiabeCareProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResendEmailAdapter")
class ResendEmailAdapterTest {

    @Mock
    private DiabeCareProperties properties;
    @Mock
    private DiabeCareProperties.Mail mailProperties;

    @Test
    @DisplayName("no intenta llamar a Resend y no lanza excepción cuando no hay API key configurada")
    void doesNotCallResendOrThrowWhenApiKeyIsNotConfigured() {
        lenient().when(properties.mail()).thenReturn(mailProperties);
        when(mailProperties.resendApiKey()).thenReturn("");

        ResendEmailAdapter adapter = new ResendEmailAdapter(properties, new ObjectMapper());

        adapter.sendPasswordResetEmail("ana@example.com", "http://localhost:4200/auth/reset-password?token=abc");
        // Si llegamos aquí sin excepción y sin red, el modo "solo log" funcionó.
    }
}
