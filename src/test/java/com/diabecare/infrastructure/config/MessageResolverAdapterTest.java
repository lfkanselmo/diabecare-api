package com.diabecare.infrastructure.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageResolverAdapter")
class MessageResolverAdapterTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private MessageResolverAdapter adapter;

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("resuelve el mensaje usando la clave, los argumentos y el locale del contexto actual")
    void resolvesMessageUsingKeyArgsAndCurrentLocale() {
        LocaleContextHolder.setLocale(new Locale("es"));
        when(messageSource.getMessage("cycle.guidance.ovulation", new Object[]{}, new Locale("es")))
                .thenReturn("Cuidado especial durante la ovulación");

        String result = adapter.resolve("cycle.guidance.ovulation");

        assertThat(result).isEqualTo("Cuidado especial durante la ovulación");
    }

    @Test
    @DisplayName("propaga los argumentos varargs correctamente a MessageSource")
    void propagatesVarargsToMessageSource() {
        LocaleContextHolder.setLocale(new Locale("es"));
        when(messageSource.getMessage(eq("alert.cycle.open-too-long.message"), any(), eq(new Locale("es"))))
                .thenReturn("Llevas 12 días con tu período abierto");

        String result = adapter.resolve("alert.cycle.open-too-long.message", 12);

        assertThat(result).isEqualTo("Llevas 12 días con tu período abierto");
        verify(messageSource).getMessage(
                eq("alert.cycle.open-too-long.message"), eq(new Object[]{12}), eq(new Locale("es")));
    }
}