package com.diabecare.support;

import com.diabecare.application.port.out.MessageResolverPort;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

/**
 * {@link MessageResolverPort} de prueba que resuelve contra los bundles reales
 * ({@code messages.properties} / {@code messages_en.properties}), en vez de un stub
 * que ignora la clave. Da protección real contra typos en las claves de mensajes.
 */
public final class RealMessageResolver implements MessageResolverPort {

    private final ResourceBundleMessageSource source;
    private final Locale locale;

    private RealMessageResolver(Locale locale) {
        this.locale = locale;
        this.source = new ResourceBundleMessageSource();
        this.source.setBasename("messages");
        this.source.setDefaultEncoding("UTF-8");
        // Sin esto, el resultado depende del locale por defecto de la JVM que corre
        // el test (pasa en local con default es_CO, fallaba en CI con default en_US).
        this.source.setFallbackToSystemLocale(false);
    }

    public static RealMessageResolver spanish() {
        return new RealMessageResolver(Locale.forLanguageTag("es"));
    }

    public static RealMessageResolver english() {
        return new RealMessageResolver(Locale.ENGLISH);
    }

    @Override
    public String resolve(String key, Object... args) {
        return source.getMessage(key, args, locale);
    }
}
