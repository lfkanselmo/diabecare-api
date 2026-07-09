package com.diabecare.infrastructure.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        // Sin esto, cuando no existe un archivo de locale exacto (ej. no hay
        // messages_es.properties porque el español vive en el bundle raiz),
        // Spring cae al Locale por defecto de la JVM del servidor en vez del
        // bundle raiz — en un servidor con locale por defecto no-espanol,
        // un request pidiendo espanol recibiria ingles silenciosamente.
        source.setFallbackToSystemLocale(false);
        return source;
    }
}