package com.diabecare.domain.exception;

public class GlucoseReadingNotFoundException extends DomainException {
    public GlucoseReadingNotFoundException(String id) {
        super("Lectura de glucosa no encontrada con id: " + id);
    }
}