package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.MessageResolverPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageResolverAdapter implements MessageResolverPort {

    private final MessageSource messageSource;

    @Override
    public String resolve(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}